import React from "react";
import { Box, Container } from "@mui/material";
import { createHashRouter, RouterProvider } from "react-router-dom";
// import Dashboard from "./components/Dashboard";
import LeftNav from "./components/LeftNav";
import Login from "./components/Login";
import GroupsList from "./components/modules/groups/GroupsList";
import SignUp from "./components/SignUp";
import FilesList from "./components/modules/files/FilesList";
import UsersList from "./components/modules/users/UsersList";

const routes = [
    { path: "/", element: <Login />, excludeNav: true },
    { path: "/signUp", element: <SignUp />, excludeNav: true },
    // { path: "/dashboard", element: <Dashboard /> },
    { path: "/members/list", element: <UsersList /> },
    { path: "/groups/list", element: <GroupsList /> },
    { path: "/files/list", element: <FilesList /> }
]
    .map(route => {
        const stylesWithNav = {
            height: "100vh",
            flexDirection: "row",
            flexBasis: "calc(100% - 240px)"
        };

        const stylesWithoutNav = {
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center"
        };

        return {
            ...route,
            element: (
                <Box
                    sx={{
                        height: "100vh",
                        display: "flex",
                        minWidth: "100%",
                        ...(!route.excludeNav ? stylesWithNav : stylesWithoutNav)
                    }}
                >
                    {route.excludeNav ? route.element :
                        <>
                            <LeftNav />
                            <Box ml={2} mt={10} pr={"5%"} style={{ minWidth: "100%", alignSelf: !route.excludeNav ? "flex-start" : "" }}>
                                {route.element}
                            </Box>
                        </>
                    }
                </Box>

            )
        };
    });

const Router = () => {
    const router = createHashRouter(routes);

    return (
        <Container component="main" maxWidth="100vw">
            <RouterProvider router={router} />
        </Container>
    );
};

export default React.memo(Router);
