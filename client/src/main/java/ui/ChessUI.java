package ui;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.util.ArrayList;

import static java.lang.Character.isUpperCase;
import static ui.EscapeSequences.*;

public class ChessUI {

    private static final int BOARD_SIZE = 8;

    public static String[][] getChessBoardAsArray(ChessBoard chessBoard) {
        String[][] stringBoard = new String[8][8];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                ChessPiece tempPiece = chessBoard.getPiece(new ChessPosition(i+1, j+1));
                if (tempPiece != null) {
                    stringBoard[i][j] = tempPiece.toString();
                }
            }
        }

        return stringBoard;
    }

    public static String[][] getValidMovesInArray(ArrayList<ChessMove> pieceMoves) {
        String[][] stringMoves = new String[8][8];

        ChessPosition start = pieceMoves.getFirst().getStartPosition();
        stringMoves[start.getRow() - 1][start.getColumn() - 1] = "S";

        for (ChessMove move : pieceMoves) {
            ChessPosition position = move.getEndPosition();
            stringMoves[position.getRow() - 1][position.getColumn() - 1] = "V";
        }

        return stringMoves;
    }

    public static void printChessBoard(PrintStream out, String[][] board, boolean whiteBottom) {
        printChessBoard(out, board, new String[8][8], whiteBottom);
    }

    public static void printChessBoard(PrintStream out, String[][] board, String[][] moves, boolean whiteBottom) {
        out.println();
        printTopHeader(out, whiteBottom);

        boolean firstIsWhite = true;
        for (int i = 0; i < BOARD_SIZE; i++) {
            int boardRow = whiteBottom ? (BOARD_SIZE - i - 1) : i;
            drawChessRow(out, i, board[boardRow], moves[boardRow], firstIsWhite, whiteBottom);
            firstIsWhite = !firstIsWhite;
        }

        printTopHeader(out, whiteBottom);
    }

    private static void printTopHeader(PrintStream out, boolean whiteBottom) {
        String[] columns = { " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        setGreyBG(out);
        out.print("   ");
        for (int i = 0; i < BOARD_SIZE; i++) {
            int index = whiteBottom ? i : BOARD_SIZE - i - 1;
            out.print(columns[index]);
        }
        out.print("   ");
        resetColor(out);
        out.println();
    }

    private static void drawSideHeader(PrintStream out, int col, boolean whiteBottom) {
        setGreyBG(out);
        int actual = whiteBottom ? (BOARD_SIZE - col) : (col + 1);
        out.print(" " + actual + " ");
    }

    private static void drawChessRow(PrintStream out, int col, String[] boardRow, String[] movesRow, boolean firstIsWhite, boolean whiteBottom) {
        drawSideHeader(out, col, whiteBottom);
        boolean isWhite = firstIsWhite;
        for (int i = 0; i < BOARD_SIZE; i++) {
            int boardCol = whiteBottom ? i : (BOARD_SIZE - i - 1);
            drawChessSquare(out, boardRow[boardCol], movesRow[boardCol], isWhite);
            isWhite = !isWhite;
        }
        drawSideHeader(out, col, whiteBottom);
        resetColor(out);
        out.println();
    }

    private static void drawChessSquare(PrintStream out, String pieceString, String moveString, boolean isWhite) {
        boolean isStart = false;
        boolean toHighlight = false;

        if (moveString != null) {
            if (moveString.equals("S")) {
                isStart = true;
            } else {
                toHighlight = true;
            }
        }

        if (isStart) {
            printStartMoveSquare(out, pieceString);
        } else if (isWhite) {
            printWhiteSquare(out, pieceString, toHighlight);
        } else {
            printBlackSquare(out, pieceString, toHighlight);
        }
    }

    private static void printWhiteSquare(PrintStream out, String pieceString, boolean highlighted) {
        if (highlighted) {
            setBlackText(out);
            setLightGreenBG(out);
        } else {
            setWhiteBG(out);
        }
        if (pieceString != null) {
            if (isUpperCase(pieceString.charAt(0))) {
                setRedText(out);
            } else {
                setBlueText(out);
            }
            out.print(" "+pieceString.toUpperCase()+" ");
        } else {
            out.print("   ");
        }
    }

    private static void printBlackSquare(PrintStream out, String pieceString, boolean highlighted) {
        if (highlighted) {
            setBlackText(out);
            setDarkGreenBG(out);
        } else {
            setBlackBG(out);
        }
        if (pieceString != null) {
            if (isUpperCase(pieceString.charAt(0))) {
                setRedText(out);
            } else {
                setBlueText(out);
            }
            out.print(" "+pieceString.toUpperCase()+" ");
        } else {
            out.print("   ");
        }
    }

    private static void printStartMoveSquare(PrintStream out, String pieceString) {
        setYellowBG(out);
        setBlackText(out);
        if (pieceString != null) {
            out.print(" "+pieceString.toUpperCase()+" ");
        } else {
            out.print("   ");
        }
    }

    public static void setRedText(PrintStream out) {
        out.print(SET_TEXT_COLOR_RED);
    }

    private static void setBlueText(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLUE);
    }

    private static void setBlackText(PrintStream out) {
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setBlackBG(PrintStream out) {
        out.print(SET_BG_COLOR_BLACK);
    }

    private static void setWhiteBG(PrintStream out) {
        out.print(SET_BG_COLOR_WHITE);
    }

    private static void setGreyBG(PrintStream out) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setLightGreenBG(PrintStream out) {
        out.print(SET_BG_COLOR_GREEN);
    }

    private static void setDarkGreenBG(PrintStream out) {
        out.print(SET_BG_COLOR_DARK_GREEN);
    }

    private static void setYellowBG(PrintStream out) {
        out.print(SET_BG_COLOR_YELLOW);
    }

    public static void resetColor(PrintStream out) {
        out.print(UNSET_BG_COLOR);
        out.print(UNSET_TEXT_COLOR);
    }
}
