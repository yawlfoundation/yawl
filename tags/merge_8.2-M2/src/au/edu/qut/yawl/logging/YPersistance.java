/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of 
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.logging;


public class YPersistance {

//    private int maxcase = 0;
//
//    static Vector runners = new Vector();
//    private static YPersistance _self = null;
//
//    private boolean enabled = false;
//    private boolean database_exists = false;
//
//    private Configuration cfg = null;
//    public SessionFactory factory = null;
//
//    private HashMap runnermap = null;
//
//    private Map idtoid = new HashMap();
//
//    public static YPersistance getInstance() {
//	if (_self==null)
//	    _self = new YPersistance();
//	return _self;
//    }
//
//    public static void stopthis() {
//	for (int i = 0; i < runners.size(); i++) {
//	    YNetRunner runner = (YNetRunner) runners.get(i);
//
//	    runner.stop();
//
//	}
//    }
//
//    private YPersistance() {
//	if (InterfaceB_EngineBasedServer.db != null && InterfaceB_EngineBasedServer.db.equals("1")) {
//	    database_exists = true;
//	}
//
//	if (database_exists)
//	    {
//		try {
//
//		    cfg = new Configuration();
//		    cfg.addClass(YSpecFile.class);
//		    cfg.addClass(YNetRunner.class);
//		    cfg.addClass(YWorkItem.class);
//		    cfg.addClass(P_YIdentifier.class);
//		    cfg.addClass(YCaseData.class);
//		    cfg.addClass(YLogData.class);
//		    cfg.addClass(YWorkItemEvent.class);
//		    cfg.addClass(YLogIdentifier.class);
//		    cfg.addClass(User.class);
//		    cfg.addClass(YAWLServiceReference.class);
//
//		    factory = cfg.buildSessionFactory();
//
//		    System.out.println("\nChecking for Existing Database Tables...\n");
//
//		    boolean createtables = false;
//		    Connection con = null;
//
//		    /*
//		      Execute a select statement to see if tables are there
//		     */
//		    Session session = factory.openSession();
//		    try {
//			con = session.connection();
//			if (con != null) {
//			    Statement st = con.createStatement();
//			    ResultSet rs = st.executeQuery("select * from specs");
//			}
//
//		    } catch (Exception e) {
//			createtables = true;
//		    }
//
//		    if (createtables) {
//			System.out.println("TABLES DOES NOT EXIST...CREATING....");
//			new SchemaUpdate(cfg).execute(false , true);
//		    }
//
//		    session.close();
//		    //factory = cfg.buildSessionFactory();
//
//
//		} catch (Exception e) {
//		    e.printStackTrace();
//		    enabled = false;
//		    database_exists = false;
//		}
//	    }
//    }
//
//    public void restore(YEngine engine) {
//
//	if (!database_exists)
//	    return;
//
//
//
//	try {
//
//	    Session session = factory.openSession();
//
//	    Query query = session.createQuery("from au.edu.qut.yawl.authentication.User");
//
//	    for (Iterator it = query.iterate(); it.hasNext();) {
//		User user = (User) it.next();
//		if (!user.getUserID().equals("admin")) {
//		    UserList.getInstance().addUser(user.getUserID(),
//						   user.getPassword(),
//						   user.getIsAdmin());
//		}
//	    }
//
//	    query = session.createQuery("from au.edu.qut.yawl.elements.YAWLServiceReference");
//
//	    for (Iterator it = query.iterate(); it.hasNext();) {
//		YAWLServiceReference service = (YAWLServiceReference) it.next();
//
//		if (!service.getURI().startsWith("http://localhost:8080/yawlWSInvoker/")) {
//		    engine.addYawlService(service);
//		}
//	    }
//
//	    query = session.createQuery("from au.edu.qut.yawl.engine.YSpecFile");
//
//	    for (Iterator it = query.iterate(); it.hasNext();) {
//		YSpecFile spec = (YSpecFile) it.next();
//		String xml = spec.getXML();
//
//		File f = new File("restore.xml");
//		BufferedWriter buf = new BufferedWriter(new FileWriter(f));
//		buf.write(xml,0,xml.length());
//		buf.close();
//
//		engine.addSpecifications(f.getAbsolutePath());
//	    }
//
//	    System.out.println("Trying to restore net runners");
//	    query = session.createQuery("from au.edu.qut.yawl.engine.YNetRunner");
//	    for (Iterator it = query.iterate(); it.hasNext();) {
//		YNetRunner runner = (YNetRunner) it.next();
//		runners.add(runner);
//	    }
//
//	    HashMap map = new HashMap();
//	    for (int i = 0; i < runners.size(); i++) {
//		YNetRunner runner = (YNetRunner) runners.get(i);
//		String id = runner.get_caseID();
//		query = session.createQuery("select from au.edu.qut.yawl.engine.YLogIdentifier where case_id = "+id);
//		for (Iterator it = query.iterate(); it.hasNext();) {
//		    YLogIdentifier ylogid = (YLogIdentifier) it.next();
//		    map.put(ylogid.getIdentifier(),ylogid);
//		}
//	    }
//	    YawlLogServletInterface.getInstance().setListofcases(map);
//
//	    runnermap = new HashMap();
//
//	    int checkedrunners = 0;
//
//	    Vector storedrunners = (Vector) runners.clone();
//
//	    while (checkedrunners < runners.size()) {
//
//		for (int i = 0; i < runners.size();i++) {
//		    YNetRunner runner = (YNetRunner) runners.get(i);
//
//		    P_YIdentifier pid = runner.get_standin_caseIDForNet();
//		    if (runner.getContainingTaskID()==null) {
//
//			//This is a root net runner
//
//			YSpecification specification = YEngine.getInstance().getSpecification(runner.getYNetID());
//			if (specification != null) {
//			    YNet net = (YNet) specification.getRootNet().clone();
//			    runner.setNet(net);
//
//			    runnermap.put(runner.get_standin_caseIDForNet().toString(),runner);
//			} else {
//			    /* This occurs when a specification has been unloaded, but the case is still there
//			       This case is not persisted, since we must have the specification stored as well.
//			     */
//			    removeData(runner);
//			    storedrunners.remove(runner);
//
//			}
//			checkedrunners++;
//
//		    } else {
//
//
//			//This is not a root net, but a decomposition
//
//			// Find the parent runner
//			String myid = runner.get_standin_caseIDForNet().toString();
//			String parentid = myid.substring(0,myid.lastIndexOf("."));
//
//
//			YNetRunner parentrunner = (YNetRunner) runnermap.get(parentid);
//
//			if (parentrunner!=null) {
//			    YNet parentnet = parentrunner.getNet();
//
//			    YCompositeTask task = (YCompositeTask) parentnet.getNetElement(runner.getContainingTaskID());
//			    runner.setTask(task);
//
//			    YNet net = (YNet) task.getDecompositionPrototype().clone();
//			    runner.setNet(net);
//			    runnermap.put(runner.get_standin_caseIDForNet().toString(),runner);
//
//			    checkedrunners++;
//			}
//
//		    }
//		}
//	    }
//
//	    runners = storedrunners;
//
//	    for (int i = 0; i < runners.size();i++) {
//
//		YNetRunner runner = (YNetRunner) runners.get(i);
//
//		YNet net = runner.getNet();
//
//
//		P_YIdentifier pid = runner.get_standin_caseIDForNet();
//
//		if (runner.getContainingTaskID()==null) {
//		    // This is a root net runner
//
//		    YIdentifier id = restoreYID(pid,null,runner.getYNetID(),net);
//		    runner.set_caseIDForNet(id);
//
//		}
//
//		YWorkItemRepository.getInstance().setNetRunnerToCaseIDBinding(runner,runner.get_caseIDForNet());
//		engine.addRunner(runner);
//
//
//		Set busytasks = runner.getBusyTaskNames();
//
//		for (Iterator busyit = busytasks.iterator(); busyit.hasNext();) {
//		    String name = (String) busyit.next();
//		    YExternalNetElement element = net.getNetElement(name);
//
//		    runner.addBusyTask(element);
//		}
//
//		Set enabledtasks = runner.getEnabledTaskNames();
//
//		for (Iterator enabit = enabledtasks.iterator(); enabit.hasNext();) {
//		    String name = (String) enabit.next();
//		    YExternalNetElement element = net.getNetElement(name);
//
//		    if (element instanceof YTask) {
//			YTask externalTask = (YTask) element;
//			runner.addEnabledTask(externalTask);
//		    }
//
//		}
//
//
//
//
//	    }
//
// 	    query = session.createQuery("from au.edu.qut.yawl.engine.YWorkItem");
//	    for (Iterator it = query.iterate(); it.hasNext();) {
//		YWorkItem witem = (YWorkItem) it.next();
//
//		if (witem.getStatus().equals(witem.statusEnabled)) {
//		    witem.setStatus(witem.statusEnabled);
//		}
//		if (witem.getStatus().equals(witem.statusFired)) {
//		    witem.setStatus(witem.statusFired);
//		}
//		if (witem.getStatus().equals(witem.statusExecuting)) {
//		    witem.setStatus(witem.statusExecuting);
//		}
//		if (witem.getStatus().equals(witem.statusComplete)) {
//		    witem.setStatus(witem.statusComplete);
//		}
//		if (witem.getStatus().equals(witem.statusIsParent)) {
//		    witem.setStatus(witem.statusIsParent);
//		}
//		if (witem.getStatus().equals(witem.statusDeadlocked)) {
//		    witem.setStatus(witem.statusDeadlocked);
//		}
//		if (witem.getStatus().equals(witem.statusDeleted)) {
//		    witem.setStatus(witem.statusDeleted);
//		}
//
//		if (witem.getData_string()!=null) {
//		    StringReader reader = new StringReader(witem.getData_string());
//		    SAXBuilder builder = new SAXBuilder();
//		    Document data = builder.build(reader);
//		    witem.setInitData(data.getRootElement());
//		}
//
//		java.util.StringTokenizer st = new java.util.StringTokenizer(witem.getThisId(),":");
// 		String caseandid = st.nextToken();
// 		java.util.StringTokenizer st2 = new java.util.StringTokenizer(caseandid,".");
// 		String caseid = st2.nextToken();
// 		String taskid = st.nextToken();
//
// 		YIdentifier workitemid = (YIdentifier) idtoid.get(caseandid);
//		if (workitemid!=null) {
//		    witem.setSource(new YWorkItemID(workitemid,taskid));
//		    witem.addToRepository();
//		} else {
//		    session.delete(witem);
//		}
//
//
//
//	    }
//	    session.close();
//
//	    /*
//	      Start net runners. This is a restart of a NetRunner not a clean start, therefore, the net runner should not create any new work items, if they have already been created.
//	     */
//
//
//	    for (int i = 0; i < runners.size(); i++) {
//		YNetRunner runner = (YNetRunner) runners.get(i);
//
//		runner.start();
//
//
//	    }
//
//	    enabled = true;
//	    System.out.println("Persistance Module Started...All ok");
//
//	} catch (Exception e) {
//
//	    enabled = false;
//	    e.printStackTrace();
//	}
//
//    }
//
//    public synchronized void removeData(Object o) {
//
// 	if (enabled) {
//	    try{
//		if (factory!=null) {
//		    Session s = factory.openSession();
//		    Transaction tx= s.beginTransaction();
//		    s.delete(o);
//		    tx.commit();
//		    s.close();
//
//		}
//	    } catch (Exception e) {
//		System.out.println("Deleting Failed");
//	    }
//	}
//    }
//
//    public synchronized void clearCase(YIdentifier id) {
//	Object o = id;
// 	if (enabled) {
//	    try{
//		if (factory!=null) {
//		    Session s = factory.openSession();
//		    Transaction tx= s.beginTransaction();
//		    try {
//			List list = id.get_children();
//			for (int i = 0; i < list.size(); i++) {
//			    YIdentifier child = (YIdentifier) list.get(i);
//			    clearCase(child);
//			}
//
//			P_YIdentifier py = createPY(o);
//			o = py;
//
//
//			boolean runnerfound = false;
//			Query query = s.createQuery("from au.edu.qut.yawl.engine.YNetRunner where case_id = '"+id.toString()+"'");
//			for (Iterator it = query.iterate(); it.hasNext();) {
//			    YNetRunner runner = (YNetRunner) it.next();
//			    s.delete(runner);
//			    runnerfound = true;
//			}
//			if (!runnerfound) {
//			    s.delete(o);
//			}
//
//			tx.commit();
//			s.close();
//		    } catch (Exception e) {
//			s.close();
//			System.out.println("Clearing Failed");
//			e.printStackTrace();
//		    }
//		}
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    }
//
//	}
//
//    }
//
//    public void storeData(Object o) {
//	synchronized(this) {
//	if (enabled) {
//	    try{
//		if (factory!=null) {
//		    Session s = factory.openSession();
//		    Transaction tx= s.beginTransaction();
//		    try {
//			if (o.getClass().getName().endsWith("YIdentifier")) {
//
//
//			    P_YIdentifier py = createPY(o);
//			    o = py;
//			}
//			else if (o.getClass().getName().endsWith("YNetRunner")) {
//			    YNetRunner runner = (YNetRunner) o;
//
//				P_YIdentifier py = createPY(runner.get_caseIDForNet());
//
//			    runner.set_standin_caseIDForNet(py);
//			}
//
//			s.save(o);
//			tx.commit();
//			s.close();
//
//		    } catch (Exception e) {
//			s.close();
//			System.out.println("Storing Failed: " + e);
//		    }
//		}
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    }
//
//	}
//	}
//    }
//
//    public void updateData(Object o) {
//	synchronized(this) {
//	if (enabled) {
//	    try{
//		if (factory!=null) {
//		    Session s = factory.openSession();
//		    Transaction tx= s.beginTransaction();
//		    try {
//			if (o.getClass().getName().endsWith("YIdentifier")) {
//
//			    o = createPY(o);
//			}
//			else if (o.getClass().getName().endsWith("YNetRunner")) {
//			    YNetRunner runner = (YNetRunner) o;
//			    P_YIdentifier py = createPY(runner.get_caseIDForNet());
//			    runner.set_standin_caseIDForNet(py);
//			}
//			s.update(o);
//			tx.commit();
//			s.close();
//
//		    } catch (net.sf.hibernate.HibernateException e2) {
//			tx.commit();
//			s.close();
//			System.out.println("Updating Failed");
//
//		    }
//		}
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    }
//
//	}
//}
//    }
//
//    public P_YIdentifier createPY(Object o) {
//	YIdentifier yid = (YIdentifier) o;
//	P_YIdentifier py = new P_YIdentifier();
//	py.set_idString(yid.get_idString());
//	List list = yid.get_children();
//	List plist = new Vector();
//
//	py.setLocationNames(yid.getLocationNames());
//
//	/*
//	  The children must be p_yidentifiers instead of normal yidentifiers
//	*/
//	for (int i = 0; i < list.size(); i++) {
//	    YIdentifier child = (YIdentifier) list.get(i);
//	    plist.add(createPY(child));
//	}
//	py.set_children(plist);
//
//	return py;
//    }
//
//
//    public YIdentifier restoreYID(P_YIdentifier pid, YIdentifier father, String specname, YNet net) {
//	YIdentifier id = new YIdentifier(pid.toString());
//
//	YNet sendnet = net;
//
//	id.set_father(father);
//
//	List list = pid.get_children();
//
//	if (list.size()>0) {
//	    List idlist = new Vector();
//
//	    for (int i = 0; i <list.size();i++) {
//		P_YIdentifier child = (P_YIdentifier) list.get(i);
//
//		YNetRunner netRunner = (YNetRunner) runnermap.get(child.toString());
//		if (netRunner!=null) {
//		    sendnet = netRunner.getNet();
//		}
//		YIdentifier caseid = restoreYID(child,id,specname,sendnet);
//
//		if (netRunner!=null)
//		    netRunner.set_caseIDForNet(caseid);
//
//		idlist.add(caseid);
//	    }
//
//	    id.set_children(idlist);
//	}
//
//
//	for (int i = 0; i < pid.getLocationNames().size();i++) {
//
//	    String name = (String) pid.getLocationNames().get(i);
//	    YExternalNetElement element = net.getNetElement(name);
//
//	    if (element==null) {
//		name = name.substring(0,name.length()-1);
//		String[] splitname = name.split(":");
//
//
//		/*
//		  Get the task associated with this condition
//		*/
//		YTask task = null;
//		if (name.indexOf("CompositeTask")!=-1) {
//		    YNetRunner netRunner_temp = (YNetRunner) runnermap.get(father.toString());
//		    task = (YTask)netRunner_temp.getNet().getNetElement(splitname[1]);
//		} else {
//		    task = (YTask)net.getNetElement(splitname[1]);
//		}
//		if(task!=null)
//		    {
//			YInternalCondition condition;
//			if(splitname[0].startsWith(YInternalCondition._mi_active))
//			    {
//
//				condition = task.getMIActive();
//				condition.add(id);
//
//			    }
//			else if(splitname[0].startsWith(YInternalCondition._mi_complete))
//			    {
//
//				condition = task.getMIComplete();
//				condition.add(id);
//
//			    }
//			else if(splitname[0].startsWith(YInternalCondition._mi_entered))
//			    {
//
//				condition = task.getMIEntered();
//				condition.add(id);
//
//			    }
//			else if (splitname[0].startsWith(YInternalCondition._executing))
//			    {
//
//				condition = task.getMIExecuting();
//				condition.add(id);
//
//			    }
//			else
//			    {
//
//				String msg = "Unknown YInternalCondition state";
//			    }
//		    }
//		else {
//		    if (splitname[0].startsWith("InputCondition")) {
//			net.getInputCondition().add(id);
//		    }
//		    else if (splitname[0].startsWith("OutputCondition")) {
//			net.getOutputCondition().add(id);
//		    }
//		}
//	    }
//	    else {
//		if (element instanceof YTask) {
//		    ((YTask) element).setI(id);
//		    ((YTask) element).prepareDataDocsForTaskOutput();
//
//		}
//		else if (element instanceof YCondition) {
//
//		    YConditionInterface cond = (YConditionInterface) element;
//
//		    ((YConditionInterface) element).add(id);
//		}
//	    }
//
//
//	}
//
//	idtoid.put(id.toString(),id);
//	return id;
//    }
//
//    public String getMaxCase() {
//	if (!enabled) {
//	    maxcase++;
//	    return new Integer(maxcase).toString();
//	}
//
//	try {
//	    Session session = factory.openSession();
//
//	    Query query = session.createQuery("select from YLogIdentifier order by created desc");
//	    for (Iterator it = query.iterate(); it.hasNext();) {
//		YLogIdentifier logid = (YLogIdentifier) it.next();
//		session.close();
//		return logid.getIdentifier();
//	    }
//	    session.close();
//	    return "0";
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    return "-1";
//	}
//    }
}

