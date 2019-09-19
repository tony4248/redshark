package com.redshark.texas;

public class CommandType {
	public final static String BE_READY_REQ = "Be.Ready.Req"; //用户点击准备后发送的请求
	public final static String PICK_DEALER_RES = "Pick.Dealer.Res"; //选出庄家
	public final static String START_BLIND_BET_RES = "Start.Blind.Bet.Res"; //下小盲注和大盲注
	public final static String DEAL_BOARD_POKERS_RES = "Deal.Board.Pokers.Res"; //发公共牌
	public final static String DEAL_POCKET_POKERS_RES = "Deal.Pocket.Pokers.Res"; //发个人的牌
	public final static String PUT_BETS_REQ = "Put.Bets.Req"; //出牌下注的请求
	public final static String PUT_BETS_RES = "Put.Bets.Res"; //出牌下注的的命令
	public final static String MOVE_CHIPS_TO_POT_RES = "Move.Chips.To.Pot.Res"; //将投注的筹码加入底池
	public final static String SHOW_DOWN_RES = "Show.Down.Res"; //子游戏结束的命令
	public final static String PLAY_GAME_AGAIN_REQ = "Play.Game.Again.Req"; //在来一局的请求
	public final static String PLAY_GAME_AGAIN_RES = "Play.Game.Again.Res"; //在来一局的命令
	public final static String SUB_GAME_END_RES = "Sub.Game.End.Res"; //子游戏结束的命令
	public final static String GAME_END_RES = "Game.End.Res"; //游戏结束的命令
	public final static String GAME_END_REQ = "Game.End.Req"; //游戏结束的命令
}
