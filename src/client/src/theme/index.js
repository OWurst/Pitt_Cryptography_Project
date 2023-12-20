import { createTheme } from "@mui/material";

const palette = {
    primary: {
        main: "rgb(0,118,152)"
    },
    secondary: {
        main: "rgb(132,142,153)"
    }
};

const typography = {
    h1: {
        fontWeight: 500,
        fontSize: "35px"
    },
    h2: {
        fontWeight: 500,
        fontSize: "29px"
    },
    h3: {
        fontWeight: 500,
        fontSize: "24px"
    },
    h4: {
        fontWeight: 500,
        fontSize: "20px"
    },
    h5: {
        fontWeight: 500,
        fontSize: "16px"
    },
    h6: {
        fontWeight: 500,
        fontSize: "14px"
    }
};

const theme = createTheme({
    palette,
    typography
});
export default theme;
