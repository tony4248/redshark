package com.redshark;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.jongo.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.redshark.entity.Session.Status;
import com.redshark.data.RoomDao;
import com.redshark.data.RoomPojo;
import com.redshark.data.UserDao;
import com.redshark.data.UserPojo;
import com.redshark.ddz.Poker;
import com.redshark.ddz.PokerUtils;
import com.redshark.entity.Room;
import com.redshark.entity.Sessions;
import com.redshark.entity.Table;
import com.redshark.entity.User;
import com.redshark.entity.UserSession;
import com.redshark.event.Event;
import com.redshark.event.EventDispatcher;
import com.redshark.event.EventFactory;
import com.redshark.event.EventType;
import com.redshark.event.ExecutorEventDispatcher;
import com.redshark.texas.BetRound;
import com.redshark.texas.Deck;
import com.redshark.texas.ExtraAttrUtils;
import com.redshark.texas.Hand;
import com.redshark.texas.HandPool;
import com.redshark.texas.Pot;
import com.redshark.util.CommonUtil;

public class Test {
	
	public void texas() {
		Deck deck = new Deck();
		deck.shuffle();
		List<com.redshark.texas.Poker> boardCards = new ArrayList<com.redshark.texas.Poker>();
		boardCards.add(deck.drawCard());
		boardCards.add(deck.drawCard());
		boardCards.add(deck.drawCard());
		boardCards.add(deck.drawCard());
		boardCards.add(deck.drawCard());
		System.out.println("board Cards:" + boardCards.toString());
		List<com.redshark.texas.Poker> pocketCards1 = new ArrayList<com.redshark.texas.Poker>();
		pocketCards1.add(deck.drawCard());
		pocketCards1.add(deck.drawCard());
		List<com.redshark.texas.Poker> cards1 = new ArrayList<com.redshark.texas.Poker>();
	    cards1.addAll(pocketCards1);
	    cards1.addAll(boardCards);
	    Hand bestHand1 = new HandPool(cards1).getBestHand();
		List<com.redshark.texas.Poker> pocketCards2 = new ArrayList<com.redshark.texas.Poker>();
		pocketCards2.add(deck.drawCard());
		pocketCards2.add(deck.drawCard());
		List<com.redshark.texas.Poker> cards2 = new ArrayList<com.redshark.texas.Poker>();
	    cards2.addAll(pocketCards2);
	    cards2.addAll(boardCards);	   
	    Hand bestHand2 = new HandPool(cards2).getBestHand();
		List<com.redshark.texas.Poker> pocketCards3 = new ArrayList<com.redshark.texas.Poker>();
		pocketCards3.add(deck.drawCard());
		pocketCards3.add(deck.drawCard());
		List<com.redshark.texas.Poker> cards3 = new ArrayList<com.redshark.texas.Poker>();
	    cards3.addAll(pocketCards3);
	    cards3.addAll(boardCards);
	    Hand bestHand3 = new HandPool(cards3).getBestHand();
		List<com.redshark.texas.Poker> pocketCards4 = new ArrayList<com.redshark.texas.Poker>();
		pocketCards4.add(deck.drawCard());
		pocketCards4.add(deck.drawCard());
		List<com.redshark.texas.Poker> cards4 = new ArrayList<com.redshark.texas.Poker>();
	    cards4.addAll(pocketCards4);
	    cards4.addAll(boardCards);
	    Hand bestHand4 = new HandPool(cards4).getBestHand();
	    System.out.println("pocketCards1:" + pocketCards1.toString());
	    System.out.println("cards1" + cards1.toString());
	    System.out.println("u1 BestHand" + bestHand1.toString());
	    System.out.println("pocketCards2:" + pocketCards2.toString());
	    System.out.println("cards2" + cards2.toString());
	    System.out.println("u2 BestHand" + bestHand2.toString());
	    System.out.println("pocketCards3:" + pocketCards3.toString());
	    System.out.println("cards3" + cards3.toString());
	    System.out.println("u3 BestHand" + bestHand3.toString());
	    System.out.println("pocketCards4:" + pocketCards4.toString());
	    System.out.println("cards4" + cards4.toString());
	    System.out.println("u4 BestHand" + bestHand4.toString());
	    User user1 = new User();
	    user1.setId("1871D");
	    user1.setName("Joe");
	    User user2 = new User();
	    user2.setId("1872D");
	    user2.setName("Jack");
	    User user3 = new User();
	    user3.setId("1873D");
	    user3.setName("Tom");
	    User user4 = new User();
	    user4.setId("1874D");
	    user4.setName("Mary");
	    List<User> users = new ArrayList<>();
	    users.add(user1);
	    users.add(user2);
	    users.add(user3);
	    users.add(user4);
	    Pot pot = new Pot(users);
	    pot.addBet(user1, 5, BetRound.PREFLOP);
	    pot.addBet(user2, 10, BetRound.PREFLOP);
	    pot.addBet(user3, 10, BetRound.PREFLOP);
	    pot.addBet(user4, 20, BetRound.PREFLOP);
	    pot.addBet(user1, 15, BetRound.PREFLOP);
	    pot.addBet(user2, 10, BetRound.PREFLOP);
	    pot.addBet(user3, 10, BetRound.PREFLOP);
	    pot.addBet(user1, 40, BetRound.FLOP);
	    pot.addBet(user2, 40, BetRound.FLOP);
	    pot.addBet(user3, 35, BetRound.FLOP);
	    pot.addBet(user4, 40, BetRound.FLOP);
	    pot.addBet(user1, 50, BetRound.TURN);
	    pot.addBet(user2, 40, BetRound.TURN);
	    pot.addBet(user4, 50, BetRound.TURN);
	    pot.addBet(user1, 50, BetRound.RIVER);
	    pot.addBet(user4, 50, BetRound.RIVER);
	    HashMap<User, Hand> userBestHands = new HashMap<User, Hand>();
	    userBestHands.put(user1, bestHand1);
	    userBestHands.put(user2, bestHand2);
	    userBestHands.put(user3, bestHand3);
	    userBestHands.put(user4, bestHand4);
	    Pot.PayoutWinnerInfo winnerInfo = pot.payoutWinners(userBestHands);
	    ArrayList<Integer> potParts = winnerInfo.getPots();
		ArrayList<ArrayList<User>> potPartWinners = winnerInfo.getWinnerPerPot();
		// divide each pot part among the users that win them
		for(int i = potParts.size() - 1; i >= 0; i--)
		{
			ArrayList<User> currentPotWinners = potPartWinners.get(i);
			int currentPotSize = potParts.get(i);
			int numberOfWinners = currentPotWinners.size();
			int amountPerWinner = currentPotSize / numberOfWinners;
			int restChips = currentPotSize - (numberOfWinners * amountPerWinner);
			for (int j = 0; j < currentPotWinners.size(); j++) {
			      int chipsAwarded = amountPerWinner;
			      if (restChips > 0) {
			        chipsAwarded++;
			        restChips--;
			      }
			      System.out.println(currentPotWinners.get(j).getId() + ":" + chipsAwarded);
			}
		}
	    System.out.println(winnerInfo);
	}
	public void poker() {
		List<Integer> randomPokerIds = PokerUtils.getRandomPokerIds();
		List<Poker> list= PokerUtils.parsePokers(randomPokerIds);
		Map<Integer,List<Integer>> pokers = PokerUtils.getSplitPokerIds();
		System.out.println(list.toString());
	}
	public String toHex(String arg) throws Exception {
	    return String.format("%040x", new BigInteger(1, arg.getBytes("UTF-8")));
	}
	
	public void testEventScheduler() 
	{
		Event event = EventFactory.createTDEvent(1, null, EventType.CONNECT, null, System.currentTimeMillis(), 7 * 1000L, true);
		ExecutorEventDispatcher.getInstance().ScheduleEvent(event);
		Event xevent = EventFactory.createTDEvent(2, null, EventType.CONNECT_FAILED, null, System.currentTimeMillis(), 8 * 1000L, false);
		ExecutorEventDispatcher.getInstance().ScheduleEvent(xevent);
		Event yevent = EventFactory.createTDEvent(3, null, EventType.CONNECT_SUCCESS, null, System.currentTimeMillis(), 3 * 1000L, false);
		ExecutorEventDispatcher.getInstance().ScheduleEvent(yevent);
		ExecutorEventDispatcher.getInstance().removeEvent(event);
		
	}
	
	public void testBeanCopy()
	{
		UserDao uDao = new UserDao();
		User xuser = new User();
		xuser.setId("1874D");
		xuser.setName("Joe");
		xuser.setLevel(1);
		try {
			uDao.save(xuser);
			User joe = uDao.findOneById(xuser.getId());
			System.out.println(joe);
			joe.setLevel(2);
			uDao.updateById(xuser.getId(), joe);
			uDao.deleteByName(xuser.getName());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		UserDao userDao = new UserDao();
		UserPojo userPojo = new UserPojo();
		User user = new User();
		user.setId("1873D");
		user.setName("tom");
		RoomDao roomDao = new RoomDao();
		RoomPojo roomPojo =  new RoomPojo();
		Room room = new Room();
		room.setId("1");
		room.setStatus(Room.Status.CREATED);
		room.setTier(Room.Tier.STD);		
		try {
			CommonUtil.copyFrom(userPojo, user);
			CommonUtil.copyFrom(roomPojo, room);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(userPojo.toString());
		
		try {
			userDao.save(user);
			User nUser = userDao.findOneById(user.getId());
			userDao.deleteById(user.getId());
			//userDao.delete("name", user.getName());
			//userDao.getUsers().remove("uId", user.getId());
			roomDao.save(room);
			Room nRoom = roomDao.findOneById(room.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void testJongo()
	{
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		// get handle to "mydb" database
		DB db = mongoClient.getDB("mydb");
		Jongo jongo = new Jongo(db);
		MongoCollection users = jongo.getCollection("users");
		User tom = new User();
		tom.setName("tom");
		users.save(tom);
		User joe = new User();
		joe.setName("Joe");
		users.save(joe);
		MongoCursor<User> all = users.find("{name: 'Joe'}").as(User.class);
		User one = users.findOne("{name: 'Joe'}").as(User.class);


		
	}
	
	public void testMongoDb()
	{
	 	MongoClient mongoClient = new MongoClient("localhost", 27017);

	 	// get handle to "mydb" database
	 	MongoDatabase database = mongoClient.getDatabase("mydb");
	 	mongoClient.close();
	
	}
	public void testCode()
	{
		
		try {
			int PING_PROTO = 1 << 8 | 220; //ping消息
			int PONG_PROTO = 2 << 8 | 220; //pong消息
			int SYST_PROTO = 3 << 8 | 220; //系统消息
			int EROR_PROTO = 4 << 8 | 220; //错误消息
			int AUTH_PROTO = 5 << 8 | 220; //认证消息
			int MESS_PROTO = 6 << 8 | 220; //普通消息
			UserDao userDao = new UserDao();
			User user =  new User();
			user.setId("1");
			user.setName("wesley");
			user.setPassword("123456");
			user.setCardNum(10);
			user.setScore(5000);
			User dbUser = userDao.findOneById("69314");
			dbUser.setCardNum(10);
			dbUser.setScore(5000);
			userDao.updateById("69314", dbUser);
			String sessionId = CommonUtil.sha256(user.getName() + user.getPassword());
			System.out.println("wesley:" + sessionId);
			UserSession session = new UserSession();
			session.setId(sessionId);
			session.setUser(user);
			session.setStatus(Status.NOT_CONNECTED);
			Sessions sessions = Sessions.getInstance();
			sessions.put(sessionId, session);
			User user2 =  new User();
			user2.setId("2");
			user2.setName("tom1001");
			user2.setPassword("0921095");
			user2.setCardNum(10);
			user2.setCardNum(10);
			user2.setScore(5000);
			User dbUser2 = userDao.findOneById("345607");
			dbUser2.setCardNum(10);
			dbUser2.setScore(5000);
			userDao.updateById("345607", dbUser2);
			String sessionId2 = CommonUtil.sha256(user2.getName() + user2.getPassword());
			System.out.println("tom1001:"+ sessionId2);
			User user3 =  new User();
			user3.setId("3");
			user3.setName("boby1001");
			user3.setPassword("123456");
			user3.setCardNum(10);
			user3.setScore(5000);
			User dbUser3 = userDao.findOneById("305921");
			dbUser3.setCardNum(10);
			dbUser3.setScore(5000);
			userDao.updateById("305921", dbUser3);
			String sessionId3 = CommonUtil.sha256("boby1001" + "123456");
			System.out.println("boby1001:" + sessionId3);
			UserSession session2 = new UserSession();
			session2.setId(sessionId2);
			session2.setUser(user2);
			session2.setStatus(Status.NOT_CONNECTED);
			sessions.put(sessionId2, session2);
			UserSession session3 = new UserSession();
			session3.setId(sessionId3);
			session3.setUser(user3);
			session3.setStatus(Status.NOT_CONNECTED);
			sessions.put(sessionId3, session3);
            Set<UserSession> nSet = new HashSet<UserSession>();
            nSet.add(session);
            nSet.add(session2);
            System.out.println(nSet);
            nSet.remove(session2);
            System.out.println(nSet);
			UserSession nSession = sessions.get(sessionId);
			System.out.println(nSession.toString());
			Sessions vsessions = Sessions.getInstance();
			UserSession vSession = sessions.get(sessionId);
			System.out.println(vSession.toString());
			Table table = new Table(3);
			table.addUser(session);
			table.addUser(session2);
			table.addUser(session3);
			UserSession currentUser = table.getCurrentUser();
			UserSession previousU1 = table.getPreviousUser();
			UserSession previousU2 = table.getPreviousUser();
			UserSession previousU3 = table.getPreviousUser();
			UserSession previousU4 = table.getPreviousUser();
			UserSession nextUser = table.getNextUser();
			UserSession nextUser2 = table.getNextUser();
			UserSession nextUser3 = table.getNextUser();
			UserSession nextUser4 = table.getNextUser();
			table.removeUser(nextUser3);
			System.out.println(table.getCurrentUser().toString());
//			MsgBody  bData = new MsgBody();
//			LoginReq loginReq = new LoginReq("wesley", "123456");
//			bData.setCode(MsgCode.AUTH_CODE);
//			bData.setData(loginReq);
//			bData.setVersion("1.0.0");
//			
//			MsgProto msgProto = new MsgProto(PING_PROTO, bData);
//			String msgSerialized = MsgProto.msgEncode(msgProto);
//			MsgProto msgDecoded;
//			msgDecoded = MsgProto.msgDecode(msgSerialized);
//			int code = MsgProto.getMsgCode(msgDecoded);
//			System.out.println(msgSerialized);
//			System.out.println(code);
//			String loginReqStr = MsgFactory.createLogInReqMsg(loginReq);
//			System.out.println(loginReqStr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
