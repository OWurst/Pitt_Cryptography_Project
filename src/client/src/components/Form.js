import React from "react";
import { Alert, Avatar, Box, Button, Grid, Typography } from "@mui/material";
import { convertToCamelCase } from "../utils";

const Form = ({ fields, submitHandler, title, icon, footer, submitText = "submit", error }) => {
    const handleSubmit = React.useCallback(async e => {
        e.preventDefault();
        const data = {};
        const formData = new FormData(e.target);
        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }
        if (submitHandler && typeof submitHandler === "function") {
            await submitHandler(data);
        }
    }, [submitHandler]);

    return (
        <>
            {icon &&
                <Avatar sx={{ m: 1, bgcolor: "secondary.main" }}>
                    {icon}
                </Avatar>
            }
            {title &&
                <Typography component="h1" variant="h5">
                    {title}
                </Typography>
            }
            <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
                <Grid container spacing={2} mb={2}>
                    {fields.map(field => {
                        const { label, Field, xs = 12, md = 6, fullWidth = true, ...allOtherProps } = field;
                        const id = field.id || convertToCamelCase(label);
                        return (
                            <Grid item key={`field-grid-item-${id}`} xs={xs} md={md}>
                                <Field
                                    key={id}
                                    name={id}
                                    label={label}
                                    fullWidth={fullWidth}
                                    {...allOtherProps}
                                />
                            </Grid>
                        );
                    })}
                </Grid>
                {error && <Alert severity="error">{error}</Alert>}
                <Button
                    type="submit"
                    fullWidth
                    variant="contained"
                    sx={{ mt: 3, mb: 2 }}
                >
                    {submitText}
                </Button>
                {footer}
            </Box>
        </>
    );
};

export default React.memo(Form);
