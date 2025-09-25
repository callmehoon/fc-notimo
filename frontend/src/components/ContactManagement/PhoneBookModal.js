import React from 'react';
import { Modal, Box, Typography, List, ListItem, ListItemButton, ListItemText } from '@mui/material';

const mockPhoneBooks = [
    { id: 1, name: '개발팀' },
    { id: 2, name: '디자인팀' },
    { id: 3, name: '기획팀' },
];

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
                <List>
                    {mockPhoneBooks.map((book) => (
                        <ListItem key={book.id} disablePadding>
                            <ListItemButton onClick={() => onSelect(book)}>
                                <ListItemText primary={book.name} />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>
            </Box>
        </Modal>
    );
}
