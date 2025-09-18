import React, { useState } from 'react';
import { Box, TextField, Button, List, ListItem, ListItemText, IconButton, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';

export default function AddressBookManager() {
  const [addressBooks, setAddressBooks] = useState([
    { id: 1, name: '주소록1', memo: '회사 주소록' },
    { id: 2, name: '주소록2', memo: '' },
  ]);
  const [newAddressBookName, setNewAddressBookName] = useState('');
  const [newAddressBookMemo, setNewAddressBookMemo] = useState('');

  const handleAddAddressBook = () => {
    if (newAddressBookName.trim()) {
      setAddressBooks([...addressBooks, {
        id: addressBooks.length + 1,
        name: newAddressBookName,
        memo: newAddressBookMemo,
      }]);
      setNewAddressBookName('');
      setNewAddressBookMemo('');
    }
  };

  const handleDeleteAddressBook = (id) => {
    setAddressBooks(addressBooks.filter(ab => ab.id !== id));
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Typography variant="h6" sx={{ mb: 2 }}>주소록 입력 영역</Typography>
      <TextField
        label="주소록 이름"
        variant="outlined"
        size="small"
        value={newAddressBookName}
        onChange={(e) => setNewAddressBookName(e.target.value)}
        sx={{ mb: 1 }}
      />
      <TextField
        label="주소록 메모"
        variant="outlined"
        size="small"
        value={newAddressBookMemo}
        onChange={(e) => setNewAddressBookMemo(e.target.value)}
        sx={{ mb: 2 }}
      />
      <Button variant="contained" onClick={handleAddAddressBook} sx={{ mb: 3 }}>
        추가
      </Button>

      <Typography variant="h6" sx={{ mb: 2 }}>주소록 리스트</Typography>
      <List sx={{ flexGrow: 1, overflowY: 'auto', border: '1px solid #eee', borderRadius: '4px' }}>
        {addressBooks.map((ab) => (
          <ListItem
            key={ab.id}
            secondaryAction={
              <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteAddressBook(ab.id)}>
                <DeleteIcon />
              </IconButton>
            }
          >
            <ListItemText primary={ab.name} secondary={ab.memo} />
          </ListItem>
        ))}
      </List>
    </Box>
  );
}
