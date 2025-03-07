package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import client.websocket.ServerMessageObserver;
import com.google.gson.Gson;
import model.dataaccess.GameData;
import ui.ChessUI;
import websocket.serverMessages.ErrorMessage;
import websocket.serverMessages.LoadGameMessage;
import websocket.serverMessages.Notification;
import websocket.serverMessages.ServerMessage;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class ChessClient implements ServerMessageObserver {
    private String authToken;
    private int[] existingGames;
    private PrintStream out;
    private int currentGameID;
    private boolean whitePlayer;
    private MenuState currentState;
    private ChessGame currentGame;

    public ChessClient() {
        authToken = null;
        existingGames = null;
        currentGameID = 0;
        currentGame = null;
        whitePlayer = true;
        currentState = MenuState.PRE_LOGIN;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public String evaluate(String input) {
        String[] tokens = input.toLowerCase().split(" ");
        int command;
        try {
            command = (tokens.length > 0) ? Integer.parseInt(tokens[0]) : 0;
        } catch (NumberFormatException e) {
            help(false);
            return "Wrong option";
        }
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        switch (currentState) {
            case PRE_LOGIN -> {
                return switch (command) {
                    case 1 -> register(params);
                    case 2 -> signIn(params);
                    case 3 -> "quit";
                    default -> help(false);
                };
            }
            case POST_LOGIN -> {
                return switch (command) {
                    case 1 -> listGames();
                    case 2 -> createGame(params);
                    case 3 -> joinGame(params);
                    case 4 -> observeGame(params);
                    case 5 -> logout();
                    default -> help(false);
                };
            }
            case MID_GAME -> {
                return switch (command) {
                    case 1 -> redrawBoard();
                    case 2 -> makeMove(params);
                    case 3 -> highlightMoves(params);
                    case 4 -> leaveGame();
                    case 5 -> resignGame();
                    default -> help(false);
                };
            }
            case OBSERVING -> {
                return switch (command) {
                    case 1 -> redrawBoard();
                    case 2 -> highlightMoves(params);
                    case 3 -> leaveGame();
                    default -> help(false);
                };
            }
            default -> {
                return "Not sure how we got here";
            }
        }
    }

    private enum MenuState {
        PRE_LOGIN,
        POST_LOGIN,
        MID_GAME,
        OBSERVING
    }

    public String help(boolean simple) {
        out.println();
        if (simple) {
            switch (currentState) {
                case PRE_LOGIN -> out.print("""
                    1 - Register
                    2 - Login
                    3 - Quit
                    
                    0 - Help
                    """);
                case POST_LOGIN -> out.print("""
                    1 - List Games
                    2 - Create Game
                    3 - Join Game
                    4 - Observe Game
                    5 - Logout
                    
                    0 - Help
                    """);
                case MID_GAME -> out.print("""
                    1 - Redraw Board
                    2 - Make Move
                    3 - Highlight Legal Moves
                    4 - Leave
                    5 - Resign
                    
                    0 - Help
                    """);
                case OBSERVING -> out.print("""
                    1 - Redraw Board
                    2 - Highlight Legal Moves
                    3 - Stop watching
                    
                    0 - Help
                    """);
            }
        } else {
            switch (currentState) {
                case PRE_LOGIN -> out.print("""
                    1 - Register: creates a new user in the database. Username must be unique.
                       Format: 1 username password email
                    2 - Login: logs in to the server with a pre-registered username with its corresponding password.
                       Format: 2 username password
                    3 - Quit: exit out of the client.
                    
                    0 - Help: print this menu again. Also prints out if input is beyond what's accepted.
                    """);
                case POST_LOGIN -> out.print("""
                    1 - List Games: show all games that are currently being hosted in the server.
                    2 - Create Game: create a new game in the database with a name. The game's name can include spaces.
                       Format: 2 gameName
                    3 - Join Game: join an existing game with as a specific player color.
                       Format: 3 white/black gameID
                    4 - Observe Game: see the current state of a game without becoming a player,
                       Format: 4 gameID
                    5 - Logout: leave your current session and return to login menu.
                    
                    0 - Help: print this menu again. Also prints out if input is beyond what's accepted.
                    """);
                case MID_GAME -> out.print("""
                    1 - Redraw Board: print the board again for the current state of the game.
                    2 - Make Move: select a piece in a given position and give its ending position.
                       Please make sure the move is legal.
                       Format: 2 start end        Format positions column then row, such as G6.
                    3 - Highlight Legal Moves: select a position on the board to see all legal moves the piece in that position can make.
                       Format: 3 position        Format positions column then row, such as G6.
                    4 - Leave: leave the current game, emptying your position and allowing anyone to join. Join again to continue.
                    5 - Resign: forfeit the current game, rendering it unplayable and the opposing player as winner.
                        This action cannot be undone.
                    
                    0 - Help: print this menu again. Also prints out if input is beyond what's accepted.
                    """);
                case OBSERVING -> out.print("""
                    1 - Redraw Board: print the board again for the current state of the game.
                    2 - Highlight Legal Moves: select a position on the board to see all legal moves the piece in that position can make.
                       Format: 3 position        Format positions column then row, such as G6.
                    3 - Stop Watching: leave the current game, returning to the menu.
                    
                    0 - Help: print this menu again. Also prints out if input is beyond what's accepted.
                    """);
            }
        }
        return "Helping";
    }

    private String register(String[] params) {
        if (params.length < 3) {
            out.print("Please provide a username, password, and email.\nFormat: 1 username password email");
            return "Retry";
        }
        String username = params[0];
        String password = params[1];
        String email = params[2];

        try {
            authToken = ServerFacade.register(username, password, email).authToken();
            currentState = MenuState.POST_LOGIN;
            ServerFacade.setObserver(this);
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        }
        help(true);

        return "Welcome new user";
    }

    private String signIn(String[] params) {
        if (params.length < 2) {
            out.print("Please provide a username and password.\nFormat: 2 username password");
            return "Retry";
        }
        String username = params[0];
        String password = params[1];

        try {
            authToken = ServerFacade.login(username, password).authToken();
            currentState = MenuState.POST_LOGIN;
            ServerFacade.setObserver(this);
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        }
        help(true);

        return "Welcome back";
    }

    private String listGames() {
        ArrayList<GameData> allGames;
        try {
            allGames = ServerFacade.listGames(authToken);
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        }
        existingGames = new int[allGames.size()];
        out.print("Games:");
        int i = 0;
        for (GameData data : allGames) {
            existingGames[i] = data.gameID();
            String white = (data.whiteUsername() != null) ? data.whiteUsername() : "No one";
            String black = (data.blackUsername() != null) ? data.blackUsername() : "No one";
            out.print("\n  " + (++i) + ". " + data.gameName() + ": " + white + " vs " + black);
        }
        return "Here's the games";
    }

    private String createGame(String[] params) {
        if (params.length < 1) {
            out.print("Please provide a game ID.\\nFormat: 2 gameName");
            return "Retry";
        }
        try {
            String fullName = stringFromParams(params);
            ServerFacade.createGame(authToken, fullName);
            out.print("Game created! List all games to see it and be able to join or observe it.");
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        }
        return "Created new game";
    }

    private String joinGame(String[] params) {
        if (params.length < 2) {
            out.print("Please provide a game ID and color.\nFormat: 3 WHITE/BLACK gameID");
            return "Retry";
        }
        if (existingGames == null) {
            out.print("Please list the games before you can join!");
            return "List first";
        }
        try {
            int index = Integer.parseInt(params[1]) - 1;
            if (index >= existingGames.length) {
                out.print("That game does not exist!");
                return "Out of range";
            }
            currentGameID = existingGames[index];
            ServerFacade.joinGame(authToken, params[0], currentGameID);
            ServerFacade.joinGameWS(authToken, params[0], currentGameID);
            currentState = MenuState.MID_GAME;
            whitePlayer = params[0].equalsIgnoreCase("white");
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        } catch (NumberFormatException e) {
            help(false);
            return "Wrong option";
        }
        return "You joined";
    }

    private String observeGame(String[] params) {
        if (params.length < 1) {
            out.print("Please provide a game ID.\nFormat: 4 gameID");
            return "Retry";
        }
        if (existingGames == null) {
            out.print("Please list the games before you can join!");
            return "List first";
        }
        try {
            int index = Integer.parseInt(params[0]) - 1;
            if (index >= existingGames.length) {
                out.print("That game does not exist!");
                return "Out of range";
            }
            currentGameID = existingGames[index];
            ServerFacade.observeGameWS(authToken, currentGameID);
            currentState = MenuState.OBSERVING;
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        } catch (NumberFormatException e) {
            help(false);
            return "Wrong option";
        }
        return "You're now watching";
    }

    private String logout() {
        try {
            ServerFacade.logout(authToken);
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Error Caught";
        }
        authToken = null;
        help(true);

        return "See you later!";
    }

    private String stringFromParams(String[] params) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            result.append(params[i]);
            if (i < params.length - 1) {
                result.append(" ");
            }
        }
        return result.toString();
    }

    private String redrawBoard() {
        String[][] gameBoard = ChessUI.getChessBoardAsArray(currentGame.getBoard());
        ChessUI.printChessBoard(out, gameBoard, whitePlayer);
        return "Board Printed";
    }

    private String makeMove(String[] params) {
        if (params.length < 2) {
            out.print("""
                Please provide a start and end position.
                If a pawn is to be promoted, also provide what it should become.
                Format: 2 start end (pieceType)""");
            return "Retry";
        }
        try {
            ChessPiece.PieceType type;
            if (params.length < 3) {
                type = null;
            } else {
                type = typeFromString(params[2]);
            }
            ChessMove move = new ChessMove(positionFromString(params[0]), positionFromString(params[1]), type);
            ServerFacade.makeMove(authToken, currentGameID, move);
            return "Move made";
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Retry";
        }
    }

    private String highlightMoves(String[] params) {
        if (params.length < 1) {
            out.print("Please provide a start position.\nFormat: 3 start");
        }
        try {
            ChessPosition start = positionFromString(params[0]);
            String[][] gameBoard = ChessUI.getChessBoardAsArray(currentGame.getBoard());
            String[][] moves = ChessUI.getValidMovesInArray((ArrayList<ChessMove>) currentGame.validMoves(start));
            ChessUI.printChessBoard(out, gameBoard, moves, whitePlayer);
            return "Valid Moves shown";
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Retry";
        }
    }

    private String leaveGame() {
        try {
            ServerFacade.leaveGame(authToken, currentGameID);
            currentGameID = 0;
            currentGame = null;
            currentState = MenuState.POST_LOGIN;
            help(true);
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Caught Error";
        }
        return "Playing later";
    }

    private String resignGame() {
        try {
            ServerFacade.resignGame(authToken, currentGameID);
            currentGameID = 0;
            currentGame = null;
            currentState = MenuState.POST_LOGIN;
            help(true);
        } catch (IOException e) {
            out.print(e.getMessage());
            return "Caught Error";
        }
        return "Sore Loser";
    }

    private ChessPosition positionFromString(String moveString) throws IOException {
        if (moveString.length() != 2) {
            throw new IOException("Wrong move format!");
        }
        char column = Character.toUpperCase(moveString.charAt(0));
        char row = moveString.charAt(1);
        int colInt = switch (column) {
            case 'A' -> 1;
            case 'B' -> 2;
            case 'C' -> 3;
            case 'D' -> 4;
            case 'E' -> 5;
            case 'F' -> 6;
            case 'G' -> 7;
            case 'H' -> 8;
            default -> throw new IOException("Column does not exist!");
        };
        int rowInt = Integer.parseInt(String.valueOf(row));
        if (rowInt > 8 || rowInt < 1) throw new IOException("Row does not exist!");
        return new ChessPosition(rowInt, colInt);
    }

    private ChessPiece.PieceType typeFromString (String type) throws IOException {
        try {
            return ChessPiece.PieceType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new IOException("That Piece Type does not exist.");
        }
    }

    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case NOTIFICATION -> displayNotification((Notification) message);
            case ERROR -> displayError((ErrorMessage) message);
            case LOAD_GAME -> loadGame((LoadGameMessage) message);
        }
    }

    private void displayNotification(Notification notification) {
        out.println(notification.getNotification());
    }

    private void displayError(ErrorMessage errorMessage) {
        ChessUI.setRedText(out);
        out.println(errorMessage.getError());
        ChessUI.resetColor(out);
    }

    private void loadGame(LoadGameMessage message) {
        String gameJson = message.getGame();
        currentGame = new Gson().fromJson(gameJson, ChessGame.class);
        ChessUI.printChessBoard(out, ChessUI.getChessBoardAsArray(currentGame.getBoard()), whitePlayer);
        help(true);
    }
}
