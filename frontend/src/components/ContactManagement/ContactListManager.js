import React, { useState } from 'react';
import { Box, TextField, Button, Table, TableBody, TableCell, TableHead, TableRow, IconButton, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';

export default function ContactListManager() {
  const [contacts, setContacts] = useState([
    { id: 1, name: '수신인1', phone: '010-1111-1111', memo: '팀장님' },
    { id: 2, name: '수신인2', phone: '010-2222-2222', memo: '' },
  ]);
  const [newContactName, setNewContactName] = useState('');
  const [newContactPhone, setNewContactPhone] = useState('');
  const [newContactMemo, setNewContactMemo] = useState('');

  const handleAddContact = () => {
    if (newContactName.trim() && newContactPhone.trim()) {
      setContacts([...contacts, {
        id: contacts.length + 1,
        name: newContactName,
        phone: newContactPhone,
        memo: newContactMemo,
      }]);
      setNewContactName('');
      setNewContactPhone('');
      setNewContactMemo('');
    }
  };

  const handleDeleteContact = (id) => {
    setContacts(contacts.filter(c => c.id !== id));
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Typography variant="h6" sx={{ mb: 2 }}>연락처 입력 영역</Typography>
      <TextField
        label="수신인 이름"
        variant="outlined"
        size="small"
        value={newContactName}
        onChange={(e) => setNewContactName(e.target.value)}
        sx={{ mb: 1 }}
      />
      <TextField
        label="수신인 전화번호"
        variant="outlined"
        size="small"
        value={newContactPhone}
        onChange={(e) => setNewContactPhone(e.target.value)}
        sx={{ mb: 1 }}
      />
      <TextField
        label="수신인 메모"
        variant="outlined"
        size="small"
        value={newContactMemo}
        onChange={(e) => setNewContactMemo(e.target.value)}
        sx={{ mb: 2 }}
      />
      <Button variant="contained" onClick={handleAddContact} sx={{ mb: 3 }}>
        추가
      </Button>

      <Typography variant="h6" sx={{ mb: 2 }}>연락처 리스트</Typography>
      <Table sx={{ flexGrow: 1, overflowY: 'auto', border: '1px solid #eee', borderRadius: '4px' }}>
        <TableHead>
          <TableRow>
            <TableCell>수신인 이름</TableCell>
            <TableCell>수신인 전화번호</TableCell>
            <TableCell align="right">관리</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {contacts.map((contact) => (
            <TableRow key={contact.id}>
              <TableCell>{contact.name}</TableCell>
              <TableCell>{contact.phone}</TableCell>
              <TableCell align="right">
                <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteContact(contact.id)}>
                  <DeleteIcon />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  );
}
