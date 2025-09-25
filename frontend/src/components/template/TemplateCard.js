import * as React from 'react';
import { Card, Box, Typography, Button, CardContent } from '@mui/material';

export default function TemplateCard({ template, onUse, onDelete }) {
    const userRole = localStorage.getItem('userRole');

    const statusStyles = {
        '심사중': { bgcolor: '#e3f2fd', color: '#1565c0' },
        '심사완료': { bgcolor: '#616161', color: '#ffffff' },
        '반려': { bgcolor: 'white', color: 'black', borderTop: '1px solid #ccc' },
        '심사요청': { bgcolor: '#e8eaf6', color: '#303f9f' },
    };

    return (
        <Card
            variant="outlined"
            sx={{
                width: '100%',
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                borderColor: '#ccc',
            }}
        >
            {/* HEADER */}
            <Box sx={{ p: 1, borderBottom: '1px solid #ccc', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="h6" fontWeight="bold">{template.title}</Typography>
                <Box>
                    <Button size="small" variant="text" sx={{ color: 'black', minWidth: 'auto', p: 0.5 }}>즐겨찾기</Button>
                    {userRole === 'ADMIN' && onDelete && (
                        <Button size="small" variant="text" sx={{ color: 'red' }} onClick={() => onDelete(template.id)}>
                            삭제하기
                        </Button>
                    )}
                </Box>
            </Box>

            {/* CONTENT */}
            <CardContent sx={{ flexGrow: 1, wordBreak: 'break-word', bgcolor: '#f8f9fa', p: 2 }}>
                <Typography color="text.secondary" sx={{ mb: 2 }}>{template.content}</Typography>
                {template.buttonTitle && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 1 }}>
                        <Box sx={{
                            bgcolor: '#007bff',
                            color: 'white',
                            padding: '6px 16px',
                            borderRadius: '4px',
                            display: 'inline-block',
                            fontSize: '0.8125rem',
                            fontWeight: 500,
                        }}>
                            {template.buttonTitle}
                        </Box>
                    </Box>
                )}
            </CardContent>

            {/* FOOTER */}
            {onUse ? (
                <Box sx={{ p: 1, borderTop: '1px solid #ccc' }}>
                    <Button variant="contained" fullWidth onClick={() => onUse(template.id)}>
                        사용하기
                    </Button>
                </Box>
            ) : (
                // Fallback to status if onUse is not provided
                template.status && statusStyles[template.status] && (
                     <Box sx={{ p: 1.5, textAlign: 'center', ...statusStyles[template.status] }}>
                        <Typography variant="body2" fontWeight="bold">{template.status}</Typography>
                    </Box>
                )
            )}
        </Card>
    );
}