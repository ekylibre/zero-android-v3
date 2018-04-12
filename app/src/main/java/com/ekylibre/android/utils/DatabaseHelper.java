//package com.ekylibre.app.utils;
//
//public class DatabaseHelper {
//
//    private Dao dao;
//
//    @Inject
//    public DatabaseHelper(Database database) {
//        dao = database.getDao();
//    }
//
//    public State getState(int id) {
//        State state = dao.getState(id);
//        state.setCities(dao.getCities(id));
//        return state;
//    }
//
//    public List<State> getStates() {
//        List<State> states = dao.getStates();
//        for (State state : states) {
//            state.setCities(dao.getCities(state.getId()));
//        }
//        return states;
//    }
//
//    public void saveState(State state) {
//        dao.saveState(state);
//        dao.saveCities(state.getCities());
//    }
//
//    public void saveStates(List<State> states) {
//        dao.saveStates(states);
//        for (State state : states) {
//            dao.saveCities(state.getCities());
//        }
//    }
//}