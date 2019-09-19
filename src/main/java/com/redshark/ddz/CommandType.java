package com.redshark.ddz;

public class CommandType {
	public final static String BE_READY_REQ = "Be.Ready.Req"; //用户点击准备后发送的请求
	public final static String DEAL_POKERS_RES = "Deal.Pokers.Res"; //用户全部准备后, 初始发牌
	public final static String DEAL_POKERS_END_REQ = "Deal.Pokers.End.Req"; //确认收到发的牌
	public final static String CALL_LANDLORD_RES = "Call.Landlord.Res"; //叫地主命令
	public final static String CALL_LANDLORD_REQ = "Call.Landlord.Req"; //叫地主请求
	public final static String HAS_LANDLORD_RES = "Has.Landlord.Res"; //产生地主
	public final static String NO_LANDLORD_RES = "No.Landlord.Res"; //没有产生地主
	public final static String ADD_SCORE_RES = "Add.Score.Res"; //底分加倍的命令
	public final static String ADD_SCORE_REQ = "Add.Score.Req"; //底分加倍的请求
	public final static String BOTTOM_SCORE_RES = "Bottom.Score.Res"; //最终的底分
	public final static String PLAY_POKERS_REQ = "Play.Pokers.Req"; //出牌请求
	public final static String PLAY_POKERS_RES = "Play.Pokers.Res"; //出牌
	public final static String SUB_GAME_END_RES = "Sub.Game.End.Res"; //子游戏结束的命令
	public final static String PLAY_GAME_AGAIN_REQ = "Play.Game.Again.Req"; //在来一局的请求
	public final static String PLAY_GAME_AGAIN_RES = "Play.Game.Again.Res"; //在来一局的命令
	public final static String GAME_END_RES = "Game.End.Res"; //游戏结束的命令
	public final static String GAME_END_REQ = "Game.End.Req"; //游戏结束的命令
}
