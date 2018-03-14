package edu.utdallas.taskExecutorImpl;

import edu.utdallas.blockingFIFOImpl.BlockingFIFOImpl;
import edu.utdallas.taskExecutor.Task;
import edu.utdallas.taskExecutor.TaskExecutor;

public class TaskExecutorImpl implements TaskExecutor
{
	private BlockingFIFOImpl queue = new BlockingFIFOImpl(100);
	
	public TaskExecutorImpl(int tno) {
		// TODO Auto-generated constructor stub
		Runnable worker = new Runnable() {
			public void run()
			{
				while (true) {
					try {
						Task exeTask = queue.take();
						exeTask.execute();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		for (int i = 0; i < tno; i++) {
			Thread workerThread = new Thread(worker, "Worker"+i);
			workerThread.start();
		}
	}
	
	@Override
	public void addTask(Task task)
	{
		// TODO Complete the implementation
		try {
			queue.put(task);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
