import React from "react";
import ReactDOM from "react-dom/client";
import Router from "./router";
import CssBaseline from "@mui/material/CssBaseline";
import { ThemeProvider } from "@mui/material/styles";
import { store } from "./store";
import { Provider } from "react-redux";
import theme from "./theme";

ReactDOM.createRoot(document.getElementById("root")).render(
    <React.StrictMode>
        <Provider store={store}>
            <CssBaseline />
            <ThemeProvider theme={theme}>
                <Router />
            </ThemeProvider>
        </Provider>
    </React.StrictMode>
);
