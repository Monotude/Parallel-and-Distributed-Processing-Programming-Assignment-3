import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class TemperatureSensor implements Runnable
{
    private static final int NUM_READINGS_PER_HOUR = 60;
    private static final int NUM_SENSORS = 8;
    private static final int MAX_TEMPERATURE = 70;
    private static final int MIN_TEMPERATURE = -100;
    private static final int MINUTES_PER_READING = 1;
    private static final int INTERVAL_SIZE = 10;

    private static final List<TemperatureReading> temperatureReadings = new ArrayList<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUM_SENSORS);
    private static final CountDownLatch latch = new CountDownLatch(NUM_SENSORS);

    public static void main(String[] args)
    {
        for (int i = 0; i < NUM_SENSORS; i++)
        {
            executorService.submit(new TemperatureSensor());
        }

        try
        {
            latch.await();
            executorService.shutdown();
            compileReport();
        }
        catch (Exception e)
        {
            System.out.println("Unexpected error has occurred.");
        }
    }

    private static void compileReport()
    {
        synchronized (temperatureReadings)
        {
            Collections.sort(temperatureReadings);

            System.out.println("Top 5 highest temperatures:");
            for (int i = temperatureReadings.size() - 1; i >= temperatureReadings.size() - 5; i--)
            {
                System.out.println(temperatureReadings.get(i).temperature());
            }

            System.out.println("\nTop 5 lowest temperatures:");
            for (int i = 0; i < 5; i++)
            {
                System.out.println(temperatureReadings.get(i).temperature());
            }

            int maxInterval = getMaxInterval();
            System.out.println("\nInterval with largest temperature difference:");
            System.out.println("Start time: " + (maxInterval * MINUTES_PER_READING) + " minutes");
            System.out.println("End time: " + ((maxInterval + INTERVAL_SIZE) * MINUTES_PER_READING) + " minutes");
        }
    }

    private static int getMaxInterval()
    {
        int maxInterval = 0;
        double maxDifference = 0;
        for (int i = 0; i < NUM_READINGS_PER_HOUR - INTERVAL_SIZE; i++)
        {
            double minTemp = temperatureReadings.get(i).temperature();
            double maxTemp = temperatureReadings.get(i).temperature();
            for (int j = i + 1; j < i + INTERVAL_SIZE; j++)
            {
                minTemp = Math.min(minTemp, temperatureReadings.get(j).temperature());
                maxTemp = Math.max(maxTemp, temperatureReadings.get(j).temperature());
            }
            double difference = maxTemp - minTemp;
            if (difference > maxDifference)
            {
                maxDifference = difference;
                maxInterval = i;
            }
        }
        return maxInterval;
    }

    @Override
    public void run()
    {
        for (int i = 0; i < NUM_READINGS_PER_HOUR; i++)
        {
            double temperature = generateRandomTemperature();
            synchronized (temperatureReadings)
            {
                temperatureReadings.add(new TemperatureReading(temperature));
            }
        }
        latch.countDown();
    }

    private double generateRandomTemperature()
    {
        Random rand = new Random();
        return MIN_TEMPERATURE + (MAX_TEMPERATURE - MIN_TEMPERATURE) * rand.nextDouble();
    }
}

record TemperatureReading(double temperature) implements Comparable<TemperatureReading>
{
    @Override
    public int compareTo(TemperatureReading o)
    {
        return Double.compare(this.temperature, o.temperature);
    }
}
