package model.dataaccess;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData(int gameID, String gameName, ChessGame game) {
        this(gameID, null, null, gameName, game);
    }

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName) {
        this(gameID, whiteUsername, blackUsername, gameName, null);
    }

    public static GameData createNew(int gameID, String gameName) {
        return new GameData(gameID, gameName, new ChessGame());
    }

    public static GameData testEmpty(int gameID, String gameName) {
        return new GameData(gameID, gameName, null);
    }
}
