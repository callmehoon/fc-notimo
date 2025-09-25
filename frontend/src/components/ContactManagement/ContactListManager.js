import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Table, TableBody, TableCell, TableHead, TableRow, IconButton, Typography } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import PhoneBookModal from './PhoneBookModal';
import recipientService from '../../services/recipientService';

export default function ContactListManager({ selectedGroup }) {
  const [contacts, setContacts] = useState([]);
  const [newContactName, setNewContactName] = useState('');
  const [newContactPhone, setNewContactPhone] = useState('');
  const [newContactMemo, setNewContactMemo] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContactId, setEditingContactId] = useState(null);

  const fetchRecipients = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (workspaceId) {
      try {
        const data = await recipientService.getRecipients(workspaceId);
        setContacts(data.content);
      } catch (error) {
        console.error("Failed to fetch recipients", error);
      }
    }
  };

  useEffect(() => {
    fetchRecipients();
  }, []);

  const handleAddContact = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (newContactName.trim() && newContactPhone.trim() && workspaceId) {
      const recipientData = {
        recipientName: newContactName,
        recipientPhoneNumber: newContactPhone,
        recipientMemo: newContactMemo,
      };
      try {
        await recipientService.createRecipient(workspaceId, recipientData);
        setNewContactName('');
        setNewContactPhone('');
        setNewContactMemo('');
        fetchRecipients(); // Refetch the list
      } catch (error) {
        console.error("Failed to create recipient", error);
        alert('수신자 추가에 실패했습니다.');
      }
    }
  };

  const handleDeleteContact = async (id) => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (window.confirm('정말로 이 수신자를 삭제하시겠습니까?')) {
      try {
        await recipientService.deleteRecipient(workspaceId, id);
        setContacts(contacts.filter(c => (c.recipientId || c.id) !== id));
        alert('수신자가 삭제되었습니다.');
      } catch (error) {
        console.error('Failed to delete recipient:', error);
        alert('수신자 삭제에 실패했습니다.');
      }
    }
  };

  const handleOpenModal = (contactId) => {
    setEditingContactId(contactId);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingContactId(null);
  };

  const handleSelectGroup = (group) => {
    setContacts(contacts.map(c => 
      (c.recipientId || c.id) === editingContactId ? { ...c, group: group.name } : c
    ));
    handleCloseModal();
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

      <Typography variant="h6" sx={{ mb: 2 }}>
        {selectedGroup ? `연락처 리스트 - ${selectedGroup.name}` : '연락처 리스트 - 전체'}
      </Typography>

      <Table sx={{ flexGrow: 1, overflowY: 'auto', border: '1px solid #eee', borderRadius: '4px' }}>
        <TableHead>
          <TableRow>
            <TableCell>수신인 이름</TableCell>
            <TableCell>수신인 전화번호</TableCell>
            <TableCell>그룹</TableCell>
            <TableCell align="right">관리</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {contacts.map((contact) => (
            <TableRow key={contact.recipientId || contact.id}>
              <TableCell>{contact.recipientName || contact.name}</TableCell>
              <TableCell>{contact.recipientPhoneNumber || contact.phone}</TableCell>
              <TableCell>{contact.group || '-'}</TableCell>
              <TableCell align="right">
                <Button size="small" onClick={() => handleOpenModal(contact.recipientId || contact.id)}>그룹 연결</Button>
                <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteContact(contact.recipientId || contact.id)}>
                  <DeleteIcon />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>

      <PhoneBookModal
        open={isModalOpen}
        onClose={handleCloseModal}
        onSelect={handleSelectGroup}
      />
    </Box>
  );
}
