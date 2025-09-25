import React, { useState, useEffect } from 'react';
import { Modal, Box, Typography, List, ListItem, ListItemButton, ListItemText, CircularProgress } from '@mui/material';
import phoneBookService from '../../services/phoneBookService';

const style = {
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -50%)',
    width: 400,
    bgcolor: 'background.paper',
    border: '2px solid #000',
    boxShadow: 24,
    p: 4,
};

export default function PhoneBookModal({ open, onClose, onSelect }) {
    const [phoneBooks, setPhoneBooks] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchPhoneBooks = async () => {
        const workspaceId = localStorage.getItem('selectedWorkspaceId');
        if (workspaceId) {
            setLoading(true);
            try {
                const data = await phoneBookService.getPhoneBooks(workspaceId);
                setPhoneBooks(data);
            } catch (error) {
                console.error('Failed to fetch phonebooks:', error);
                setPhoneBooks([]);
            } finally {
                setLoading(false);
            }
        }
    };

    useEffect(() => {
        if (open) {
            fetchPhoneBooks();
        }
    }, [open]);

    const handleSelect = (phoneBook) => {
        // 백엔드 API 구조에 맞게 변환
        onSelect({
            id: phoneBook.phoneBookId,
            name: phoneBook.phoneBookName
        });
    };

    return (
        <Modal
            open={open}
            onClose={onClose}
            aria-labelledby="phonebook-modal-title"
        >
            <Box sx={style}>
                <Typography id="phonebook-modal-title" variant="h6" component="h2">
                    주소록 선택
                </Typography>
                {loading ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                        <CircularProgress />
                    </Box>
                ) : (
                    <List>
                        {phoneBooks.map((book) => (
                            <ListItem key={book.phoneBookId} disablePadding>
                                <ListItemButton onClick={() => handleSelect(book)}>
                                    <ListItemText primary={book.phoneBookName} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                        {phoneBooks.length === 0 && !loading && (
                            <ListItem>
                                <ListItemText primary="생성된 주소록이 없습니다." />
                            </ListItem>
                        )}
                    </List>
                )}
            </Box>
        </Modal>
    );
}
