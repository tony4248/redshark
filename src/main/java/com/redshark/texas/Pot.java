package com.redshark.texas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.redshark.entity.User;

/**
 * Class Pot is used for keeping track of the pot size, both from the main pot and
 * the possible side pots, and the Users that are involved in the side pots.
 */
public class Pot
{	
	//每个用户的投注数
	private Map<User, Integer> userBets;
	//奖池的总数
	private int totalPot;
	//本轮的奖池的数
	private int roundPot;
	//处在投注的那个阶段
	private BetRound round;
	
	/**
	 * Creates a Pot object, used for keeping track of the pot for a specific hand.
	 * @param users : integer array of user IDs that play in the current hand
	 */
	public Pot(Collection<User> users)
	{
		userBets = new HashMap<User, Integer>();
		Iterator<User> userIterator = users.iterator();
		while(userIterator.hasNext())
			userBets.put(userIterator.next(), 0);

		totalPot = 0;
		roundPot = 0;
		round = BetRound.PREFLOP;
	}
	
	/**
	 * retrieve the bet of a user.
	 */
	public int getBet(User user) {
		return this.userBets.get(user);
	}
	
	/**
	 * retrieve the bet of a user.
	 */
	public int getBet(String userId) {
		User user = getUserById(userId);
		if(null != user) {return userBets.get(user);}
		return 0;
	}
	
	/**
	 * retrieve the user by Id.
	 */
	private User getUserById(String userId) {
		for(Entry<User, Integer> entry : userBets.entrySet())
		{	
			if(userId == entry.getKey().getId()) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Stores the bet of a user.
	 */
	public void addBet(String userId, int size, BetRound round)
	{
		User user = getUserById(userId);
		if(null == user) {return;}
		addBet(user, size, round);
	}
	
	/**
	 * Stores the bet of a user.
	 */
	public void addBet(User user, int size, BetRound round)
	{
		userBets.put(user, userBets.get(user) + size);
		totalPot += size;
		
		// if a new round has started, then reset the round pot, else update it
		if(round.equals(this.round))
			roundPot += size;
		else
		{
			roundPot = size;
			this.round = round;
		}
	}
	
	
	/**
	 * Return value of function payoutWinners.
	 */
	public class PayoutWinnerInfo
	{
		private ArrayList<Integer> pots;
		private ArrayList<ArrayList<User>> winnerPerPot;
		public PayoutWinnerInfo(ArrayList<Integer> pots, ArrayList<ArrayList<User>> winnerPerPot)
		{
			super();
			this.pots = pots;
			this.winnerPerPot = winnerPerPot;
		}
		public ArrayList<Integer> getPots() {
			return pots;
		}
		public ArrayList<ArrayList<User>> getWinnerPerPot() {
			return winnerPerPot;
		}
	}
	
	/**
	 * Calculates for all the users which pots they win. It first calculates which main pot and side pots there are.
	 * Then it computes which user(s) win which pot. The returned ArrayList contains three objects. The first object is
	 * an ArrayList of the pot sizes represented as integers. The second object is an ArrayList of winners per pot,
	 * where each element is itself an ArrayList of winners of the corresponding pot.
	 * @param userBestHands : A map of PokerUsers paired with their corresponding hand strengths
	 */
	public PayoutWinnerInfo payoutWinners(HashMap<User, Hand> userBestHands)
	{	
		// Calculate with the involved users how much each user put in the main pot and how much per side pot
		ArrayList<Integer> involvedUserBets = new ArrayList<Integer>();
		//取得每人的投注总额
		for(Entry<User, Hand> entry : userBestHands.entrySet())
			involvedUserBets.add(userBets.get(entry.getKey()));
		//牌型-升序
		Collections.sort(involvedUserBets);
		ArrayList<Integer> potsAmountPerUser = new ArrayList<Integer>();
		int previousAmount = 0;
		for(int i = 0; i < involvedUserBets.size(); i++)
		{
			potsAmountPerUser.add(involvedUserBets.get(i) - previousAmount);
			previousAmount = involvedUserBets.get(i);
		}
		
		// Get the sizes of the main pot and the side pots
		ArrayList<User> users = new ArrayList<User>(userBestHands.keySet());
		ArrayList<Integer> pots = getPots(users);
		
		// Calculate per pot part which players are winning it
		ArrayList<ArrayList<User>> winnerPerPot = new ArrayList<ArrayList<User>>();
		int potIndex = 0;
		int sumHandledPots = 0;
		while(userBestHands.size() > 0)
		{		
			// Get out of the remaining users the user(s) that has/have the best hand
			Hand bestHand = null;
			ArrayList<User> currentBestUsers = new ArrayList<User>();
			for(Entry<User, Hand> entry : userBestHands.entrySet())
			{
				Hand uBestHand = entry.getValue();
				if (bestHand == null || bestHand.compare(uBestHand) < 0) {
			        bestHand = uBestHand;
			        currentBestUsers.clear();
			        currentBestUsers.add(entry.getKey());
		        } else if (bestHand.compare(uBestHand) == 0) {
		        	currentBestUsers.add(entry.getKey());
		        }
			}
				
			// Calculate for each user with currently the best hand in which remaining pots he is involved
			int maxPotIndex = 0;
			int maxSumHandledPots = 0;
			for(int i = 0; i < currentBestUsers.size(); i++)
			{
				int currentPotIndex = potIndex;
				int currentSumHandledPots = sumHandledPots;
				User currentUser = currentBestUsers.get(i);
				userBestHands.remove(currentUser);
				while(userBets.get(currentUser) > currentSumHandledPots)
				{
					ArrayList<User> currentPotWinners = new ArrayList<User>();
					if(currentPotIndex <= winnerPerPot.size() - 1)
					{
						currentPotWinners = winnerPerPot.get(currentPotIndex);
						currentPotWinners.add(currentUser);
						winnerPerPot.set(currentPotIndex, currentPotWinners);
					}
					else
					{
						currentPotWinners.add(currentUser);
						winnerPerPot.add(currentPotWinners);
					}
					//currentSumHandledPots += pots.get(currentPotIndex++);
					currentSumHandledPots += potsAmountPerUser.get(currentPotIndex++);
				}
				maxPotIndex = Math.max(maxPotIndex, currentPotIndex);
				maxSumHandledPots = Math.max(maxSumHandledPots, currentSumHandledPots);
			}
			potIndex = maxPotIndex;
			sumHandledPots = maxSumHandledPots;
		}
		
		return new PayoutWinnerInfo(pots, winnerPerPot);
	}
	
	
	/**
	 * Calculates the part of the current pot that a given player can win in total. A remaining stack size of the player
	 * must be given. This is useful when a player has less chips than the current raise, because the player can then
	 * only win a part of the total pot. If you know that a player has enough chips to call, then it is faster to just
	 * request the total pot size.
	 * @param user : the user to do the request for.
	 * @param maxSize : the amount of chips the given player can add before being all-in.
	 */
	public int getMaxPotToWin(User user, int chipsToAllIn)
	{
		int maxBet = userBets.get(user) + chipsToAllIn;		
		int maxPotPart = 0;
		for(Entry<User, Integer> entry : userBets.entrySet())
		{
		    int botBet = entry.getValue();
		    if(botBet < maxBet)
		    	maxPotPart += botBet;
		    else
		    	maxPotPart += maxBet;
		}		
		return maxPotPart;
	}
	
	
	/**
	 * Returns whether the pot is empty or not.
	 */
	public boolean isEmpty()
	{
		return totalPot == 0;
	}
	
	
	/**
	 * Returns the total size of the pot.
	 */
	public int getTotalPotSize()
	{
		return totalPot;
	}
	
	
	/**
	 * Returns the sum of bets in the current round.
	 */
	public int getRoundPotSize()
	{
		return roundPot;
	}
	
	
	/**
	 * Returns the size of the main pot and possible side pots, given the users that are still involved in the hand.
	 * @param involvedUsers : the users that are still in the hand
	 */
	public ArrayList<Integer> getPots(ArrayList<User> involvedUsers)
	{
		ArrayList<Integer> pots = new ArrayList<Integer>();
		Map<User, Integer> tempUserBets = new HashMap<User, Integer>(userBets);
		while(involvedUsers.size() > 0)
		{
			int lowestBet = Integer.MAX_VALUE;
			int currentPotSize = 0;
			for(int i = 0; i < involvedUsers.size(); i++)
				lowestBet = Math.min(tempUserBets.get(involvedUsers.get(i)), lowestBet);
			
			for(Entry<User, Integer> entry : tempUserBets.entrySet())
			{
			    User key = entry.getKey();
			    int value = entry.getValue();
			    int adjustment = Math.min(value, lowestBet);
			    currentPotSize += adjustment;
				tempUserBets.put(key, value - adjustment);
				if(value == adjustment)
					involvedUsers.remove(key);				
			}
			
			pots.add(currentPotSize);
		}
		return pots;
	}
}
