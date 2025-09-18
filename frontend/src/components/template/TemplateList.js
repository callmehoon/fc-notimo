import * as React from 'react';
import { Grid } from '@mui/material';
import TemplateCard from './TemplateCard';

export default function TemplateList({ templates }) {
    return (
        <Grid container spacing={3}>
            {templates.map(template => (
                <Grid item key={template.id} xs={12} sm={6} md={3} lg={3} xl={3} sx={{ display: 'flex' }}>
                    <TemplateCard template={template} />
                </Grid>
            ))}
        </Grid>
    );
}