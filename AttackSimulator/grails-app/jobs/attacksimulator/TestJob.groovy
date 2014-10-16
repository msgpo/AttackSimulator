package attacksimulator

import com.attacksimulator.RunSysLogFeeds;
import org.feedgeneratorgrails.Orders;

class TestJob {
    static triggers = {}

    def execute(context) {
        // execute job
	def orderid = Integer.parseInt(context.mergedJobDataMap.get('orderid'));
	if(orderid == null || orderid.isEmpty()){
		System.out.println("Orderid is null.. quitting execution of job!!");
		return;
	}
	def order = Orders.get(orderid);
	def secuserid = order.userid;
	def destinationip = order.destinationip;
	def destinationport = order.destinationport;
	def frequency = order.frequency;
	def feedtype = order.feedtype;
	def factorString = order.weekendfactor;
	
	RunSysLogFeeds th = new RunSysLogFeeds(secuserid, destinationip, destinationport, frequency, feedtype, orderid, factorString);
	th.start();
	order.threadid = th.getId();
	order.save(flush: true);
	
	return;
    }
}
