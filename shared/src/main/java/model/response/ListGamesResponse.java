package model.response;

import model.dataaccess.GameData;

import java.util.ArrayList;

public record ListGamesResponse(ArrayList<GameData> games) {}
