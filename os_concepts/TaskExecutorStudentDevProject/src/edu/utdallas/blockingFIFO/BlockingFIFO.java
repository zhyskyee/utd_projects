package edu.utdallas.blockingFIFO;

import edu.utdallas.taskExecutor.Task;

/***********************************************
* @author hxz174130@utdallas.edu
* 
* @date Mar 9, 2018 2:48:54 PM
* 
***********************************************/
/**
 * @author zhy
 *
 */
public interface BlockingFIFO {
	void put(Task item) throws Exception;
	Task take() throws Exception;
}
