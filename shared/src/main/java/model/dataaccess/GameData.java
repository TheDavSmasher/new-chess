package model.dataaccess;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public static GameData forList(int gameID, String whiteUsername, String blackUsername, String gameName) {
        return new GameData(gameID, whiteUsername, blackUsername, gameName, null);
    }

    public static GameData createNew(int gameID, String gameName) {
        return new GameData(gameID, null, null, gameName, new ChessGame());
    }

    public static GameData testEmpty(int gameID, String gameName) {
        return new GameData(gameID, null, null, gameName, null);
    }
}
