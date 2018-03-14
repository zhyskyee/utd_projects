package edu.utdallas.blockingFIFOImpl;

import edu.utdallas.blockingFIFO.BlockingFIFO;
import edu.utdallas.taskExecutor.Task;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Mar 9, 2018 2:57:25 PM
* 
***********************************************/
public class BlockingFIFOImpl implements BlockingFIFO {
	private int length;
	private int count;
	private Node head;
	private Node tail;
	
	private Object notFull = new Object();
	private Object notEmpty = new Object();
	
	public BlockingFIFOImpl() {
		// TODO Auto-generated constructor stub
		this.length = 100;
		this.count = 0;
 	}
	
	public BlockingFIFOImpl(int len) {
		this.length = len;
		this.count = 0;
	}
	
	private class Node {
		Task item;
		Node next;
		
		public Node(Task item) {
			// TODO Auto-generated constructor stub
			this.item = item;
			this.next = null;
		}
	}
	
	private boolean isEmpty() {
		return (head==null);
	}
	
	private void add(Task item) {
		Node tNode = tail;
		if(count < length) {
			tail = new Node(item);
			if(isEmpty()) {
				head = tail;
			} else {
				tNode.next = tail;
			}
		}	
	}
	
	private Task get() {
		Task vTask = null;
		if(isEmpty()) {
			return null;
		} else {
			vTask = head.item;
			Node tNode = head.next;
			head = tNode;
			return vTask;
		}
	}

	@Override
	public void put(Task item) throws Exception {
		// TODO Auto-generated method stub
		if (count == length) {
			synchronized (notFull) {
				notFull.wait();
			}	
		}
		
		synchronized (notEmpty) {
			add(item);
			count++;
			notEmpty.notify();
		}
	}

	@Override
	public Task take() throws Exception {
		// TODO Auto-generated method stub
		if (count <= 0 || isEmpty()) {
			synchronized (notEmpty) {
				notEmpty.wait();
			}
		}
		
		synchronized (notFull) {
			Task result = this.get();
			if (result == null) {
				//System.out.println("It should not be possible, since it's been notified but count equals to 0!");
				throw new NullPointerException();
			}
			count--;
			notFull.notify();
			return result;
		}
	}
}
