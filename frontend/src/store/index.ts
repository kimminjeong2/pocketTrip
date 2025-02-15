import { configureStore } from "@reduxjs/toolkit";
import counterReducer from "../slices/counterSlice";
import currentPageReducer from "../slices/currentPageSlice";
import ModalControlReducer from "../slices/ModalControlSlice";
import AlertControlReducer from "../slices/AlertControlSlice";
import travelReducer from "../slices/travelSlice";
import RoutePathReducer from "../slices/RoutePathSlice";
import editReducer from "../slices/editSlice";
import SaveTourDataReducer from "../slices/SaveTourDataSlice";
import UserDataReducer from "../slices/userDataSlice";

const store = configureStore({
  reducer: {
    counter: counterReducer,
    currentPage: currentPageReducer,
    modalControl: ModalControlReducer,
    AlertControl: AlertControlReducer,
    travel: travelReducer,
    prevPath: RoutePathReducer,
    edit: editReducer,
    saveTourData: SaveTourDataReducer,
    userData: UserDataReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export default store;
export type AppDispatch = typeof store.dispatch;
