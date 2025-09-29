import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Table, TableBody, TableCell, TableHead, TableRow, IconButton, Typography, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import phoneBookService from '../../services/phoneBookService';

export default function AddressBookManager({ onGroupSelect, selectedGroup }) {
  const [addressBooks, setAddressBooks] = useState([]);
  const [newAddressBookName, setNewAddressBookName] = useState('');
  const [newAddressBookMemo, setNewAddressBookMemo] = useState('');

  // 수정 모달 관련 상태
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingAddressBookId, setEditingAddressBookId] = useState(null);
  const [editAddressBookData, setEditAddressBookData] = useState({
    name: '',
    memo: ''
  });

  // API 관련 함수들
  const fetchPhoneBooks = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (workspaceId) {
      try {
        const data = await phoneBookService.getPhoneBooks(workspaceId);
        setAddressBooks(data.map(pb => ({
          id: pb.phoneBookId,
          name: pb.phoneBookName,
          memo: pb.phoneBookMemo || ''
        })));
      } catch (error) {
        console.error("Failed to fetch phonebooks", error);
        const errorMessage = error.response?.data?.message || error.message || '주소록 목록 조회에 실패했습니다.';
        alert(errorMessage);
      }
    }
  };

  useEffect(() => {
    fetchPhoneBooks();
  }, []);

  const handleAddAddressBook = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (newAddressBookName.trim() && workspaceId) {
      const phoneBookData = {
        phoneBookName: newAddressBookName,
        phoneBookMemo: newAddressBookMemo || ''
      };
      try {
        await phoneBookService.createPhoneBook(workspaceId, phoneBookData);
        setNewAddressBookName('');
        setNewAddressBookMemo('');
        fetchPhoneBooks(); // 목록 새로고침
        alert('주소록이 추가되었습니다.');
      } catch (error) {
        console.error("Failed to create phonebook", error);
        const errorMessage = error.response?.data?.message || error.message || '주소록 추가에 실패했습니다.';
        alert(errorMessage);
      }
    }
  };

  const handleDeleteAddressBook = async (id) => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (window.confirm('정말로 이 주소록을 삭제하시겠습니까?') && workspaceId) {
      try {
        await phoneBookService.deletePhoneBook(workspaceId, id);
        fetchPhoneBooks(); // 목록 새로고침
        alert('주소록이 삭제되었습니다.');
      } catch (error) {
        console.error("Failed to delete phonebook", error);
        const errorMessage = error.response?.data?.message || error.message || '주소록 삭제에 실패했습니다.';
        alert(errorMessage);
      }
    }
  };

  // 수정 모달 관련 핸들러
  const handleOpenEditModal = (addressBook) => {
    setEditAddressBookData({
      name: addressBook.name,
      memo: addressBook.memo || ''
    });
    setEditingAddressBookId(addressBook.id);
    setIsEditModalOpen(true);
  };

  const handleCloseEditModal = () => {
    setIsEditModalOpen(false);
    setEditAddressBookData({
      name: '',
      memo: ''
    });
    setEditingAddressBookId(null);
  };

  const handleUpdateAddressBook = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (editAddressBookData.name.trim() && workspaceId && editingAddressBookId) {
      try {
        // 백엔드 DTO 형식에 맞게 데이터 변환
        const updateData = {
          newPhoneBookName: editAddressBookData.name,
          newPhoneBookMemo: editAddressBookData.memo || ''
        };

        await phoneBookService.updatePhoneBook(workspaceId, editingAddressBookId, updateData);
        fetchPhoneBooks(); // 목록 새로고침
        handleCloseEditModal();
        alert('주소록이 수정되었습니다.');
      } catch (error) {
        console.error("Failed to update phonebook", error);
        const errorMessage = error.response?.data?.message || error.message || '주소록 수정에 실패했습니다.';
        alert(errorMessage);
      }
    }
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
      <Box sx={{ flexGrow: 1, overflow: 'auto', border: '1px solid #eee', borderRadius: '4px' }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              <TableCell>주소록 이름</TableCell>
              <TableCell>메모</TableCell>
              <TableCell align="right">관리</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            <TableRow>
              <TableCell
                colSpan={3}
                sx={{
                  cursor: 'pointer',
                  bgcolor: !selectedGroup ? 'action.selected' : 'transparent',
                  '&:hover': { bgcolor: 'action.hover' },
                  textAlign: 'left',
                  fontWeight: 'bold'
                }}
                onClick={() => onGroupSelect(null)}
              >
                전체
              </TableCell>
            </TableRow>
            {addressBooks.map((ab) => (
              <TableRow key={ab.id}>
                <TableCell
                  sx={{
                    cursor: 'pointer',
                    bgcolor: selectedGroup?.id === ab.id ? 'action.selected' : 'transparent',
                    '&:hover': { bgcolor: 'action.hover' }
                  }}
                  onClick={() => onGroupSelect(ab)}
                >
                  {ab.name}
                </TableCell>
                <TableCell>{ab.memo || '-'}</TableCell>
                <TableCell align="right">
                  <IconButton aria-label="edit" onClick={() => handleOpenEditModal(ab)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteAddressBook(ab.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Box>

      {/* 주소록 수정 모달 */}
      <Dialog open={isEditModalOpen} onClose={handleCloseEditModal} maxWidth="sm" fullWidth>
        <DialogTitle>주소록 수정</DialogTitle>
        <DialogContent>
          <TextField
            label="주소록 이름"
            variant="outlined"
            size="small"
            fullWidth
            value={editAddressBookData.name}
            onChange={(e) => setEditAddressBookData({...editAddressBookData, name: e.target.value})}
            sx={{ mb: 2, mt: 1 }}
          />
          <TextField
            label="주소록 메모"
            variant="outlined"
            size="small"
            fullWidth
            multiline
            rows={3}
            value={editAddressBookData.memo}
            onChange={(e) => setEditAddressBookData({...editAddressBookData, memo: e.target.value})}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEditModal}>취소</Button>
          <Button onClick={handleUpdateAddressBook} variant="contained">저장</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
