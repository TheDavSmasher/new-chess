package model.dataaccess;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData(int gameID, String gameName) {
        this(gameID, null, null, gameName, new ChessGame());
    }

    public GameData(int gameID, String gameName, ChessGame game) {
        this(gameID, null, null, gameName, game);
    }

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName) {
        this(gameID, whiteUsername, blackUsername, gameName, null);
    }
}
