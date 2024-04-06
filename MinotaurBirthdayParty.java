import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

class Present
{
    int tag;

    public Present(int tag)
    {
        this.tag = tag;
    }
}

class Node
{
    Present present;
    AtomicReference<Node> next;

    public Node(Present present)
    {
        this.present = present;
        this.next = new AtomicReference<>(null);
    }
}

class ConcurrentLinkedList
{
    private final AtomicReference<Node> head;

    public ConcurrentLinkedList()
    {
        this.head = new AtomicReference<>(null);
    }

    public void addPresent(Present present)
    {
        Node newNode = new Node(present);
        while (true)
        {
            Node currentHead = head.get();
            newNode.next.set(currentHead);
            if (head.compareAndSet(currentHead, newNode))
            {
                break;
            }
        }
    }

    public void removePresent(int tag)
    {
        Node prev = null;
        Node current = head.get();
        while (current != null)
        {
            if (current.present.tag == tag)
            {
                if (prev != null)
                {
                    prev.next.set(current.next.get());
                } else
                {
                    head.set(current.next.get());
                }
                return;
            }
            prev = current;
            current = current.next.get();
        }
    }

    public boolean containsPresent(int tag)
    {
        Node current = head.get();
        while (current != null)
        {
            if (current.present.tag == tag)
            {
                return true;
            }
            current = current.next.get();
        }
        return false;
    }
}

public class MinotaurBirthdayParty
{
    private static final int SERVANT_COUNT = 4;
    private static final int PRESENT_COUNT = 500000;

    public static void main(String[] args)
    {
        ConcurrentLinkedList linkedList = new ConcurrentLinkedList();

        ExecutorService executorService = Executors.newFixedThreadPool(SERVANT_COUNT);

        for (int i = 0; i < PRESENT_COUNT; i++)
        {
            int tag = i + 1;
            int action = new Random().nextInt(0, 3);
            executorService.submit(new Servant(linkedList, tag, action));
        }

        executorService.shutdown();

        try
        {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (Exception e)
        {
            System.out.println("Unexpected error has occurred.");
        }

        System.out.println("All presents processed.");
    }
}

class Servant implements Runnable
{
    private final ConcurrentLinkedList linkedList;
    private final int tag;
    private final int action;

    public Servant(ConcurrentLinkedList linkedList, int tag, int action)
    {
        this.linkedList = linkedList;
        this.tag = tag;
        this.action = action;
    }

    @Override
    public void run()
    {
        switch (action)
        {
            case 0:
                linkedList.addPresent(new Present(tag));
                break;
            case 1:
                linkedList.removePresent(tag);
                break;
            case 2:
                linkedList.containsPresent(tag);
                break;
            default:
                System.out.println("Invalid action");
        }
    }
}
