import React, { useState, useEffect } from 'react';
import { Box, TextField, Button, Table, TableBody, TableCell, TableHead, TableRow, IconButton, Typography, Dialog, DialogTitle, DialogContent, DialogActions, Checkbox, Pagination } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import PhoneBookModal from './PhoneBookModal';
import recipientService from '../../services/recipientService';
import phoneBookService from '../../services/phoneBookService';

export default function ContactListManager({ selectedGroup, onGroupSelect }) {
  const [contacts, setContacts] = useState([]);
  const [newContactName, setNewContactName] = useState('');
  const [newContactPhone, setNewContactPhone] = useState('');
  const [newContactMemo, setNewContactMemo] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContactId, setEditingContactId] = useState(null);

  // 수정 모달 관련 상태
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editContactData, setEditContactData] = useState({
    recipientName: '',
    recipientPhoneNumber: '',
    recipientMemo: ''
  });

  // 다중 선택 관련 상태
  const [selectedContacts, setSelectedContacts] = useState(new Set());
  const [selectAll, setSelectAll] = useState(false);

  // 페이지네이션 관련 상태 - 주소록별로 관리
  const [paginationState, setPaginationState] = useState({
    global: { currentPage: 1, totalPages: 0, totalElements: 0 }, // 전체 보기
    phonebooks: {} // 주소록 ID별 페이지 상태
  });
  const [itemsPerPage] = useState(50);

  // 현재 선택된 그룹에 따른 페이지 정보 가져오기
  const getCurrentPageInfo = () => {
    if (selectedGroup) {
      const phoneBookId = selectedGroup.id;
      return paginationState.phonebooks[phoneBookId] || { currentPage: 1, totalPages: 0, totalElements: 0 };
    } else {
      return paginationState.global;
    }
  };

  // 페이지 정보 업데이트
  const updatePageInfo = (currentPage, totalPages, totalElements) => {
    setPaginationState(prev => {
      if (selectedGroup) {
        const phoneBookId = selectedGroup.id;
        return {
          ...prev,
          phonebooks: {
            ...prev.phonebooks,
            [phoneBookId]: { currentPage, totalPages, totalElements }
          }
        };
      } else {
        return {
          ...prev,
          global: { currentPage, totalPages, totalElements }
        };
      }
    });
  };

  const fetchRecipients = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (workspaceId) {
      try {
        const currentPageInfo = getCurrentPageInfo();
        let data;
        const pageable = {
          page: currentPageInfo.currentPage - 1, // 0-based
          size: itemsPerPage
        };

        if (selectedGroup && selectedGroup.id) {
          // 특정 주소록이 선택된 경우 해당 주소록의 수신자만 조회
          console.log('Selected phonebook:', selectedGroup);
          data = await phoneBookService.getRecipientsInPhoneBook(workspaceId, selectedGroup.id, pageable);
          console.log('Phonebook recipients data:', data);
        } else {
          // 전체 수신자 조회
          console.log('Fetching all recipients');
          data = await recipientService.getRecipients(workspaceId, pageable);
          console.log('All recipients data:', data);
        }

        setContacts(data.content || []);

        // 페이지 정보 업데이트
        const totalElements = data.totalElements || 0;
        const totalPages = data.totalPages || Math.ceil(totalElements / itemsPerPage);
        updatePageInfo(currentPageInfo.currentPage, totalPages, totalElements);
      } catch (error) {
        console.error("Failed to fetch recipients", error);
        const errorMessage = error.response?.data?.message || error.message || '연락처 목록 조회에 실패했습니다.';
        alert(errorMessage);
        setContacts([]); // 에러 시 빈 배열로 초기화
        updatePageInfo(1, 0, 0);
      }
    }
  };

  useEffect(() => {
    fetchRecipients();
  }, [selectedGroup]); // selectedGroup이 변경될 때마다 다시 조회

  // 페이지 변경 핸들러
  const handlePageChange = async (event, page) => {
    const currentPageInfo = getCurrentPageInfo();
    // 페이지 정보 업데이트
    updatePageInfo(page, currentPageInfo.totalPages, currentPageInfo.totalElements);
    setSelectedContacts(new Set()); // 페이지 변경 시 선택 초기화
    setSelectAll(false);

    // 직접 데이터 로딩
    await fetchRecipientsForPage(page);
  };

  // 특정 페이지에 대한 데이터 로딩
  const fetchRecipientsForPage = async (targetPage) => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (workspaceId) {
      try {
        let data;
        const pageable = {
          page: targetPage - 1, // 0-based
          size: itemsPerPage
        };

        if (selectedGroup && selectedGroup.id) {
          data = await phoneBookService.getRecipientsInPhoneBook(workspaceId, selectedGroup.id, pageable);
        } else {
          data = await recipientService.getRecipients(workspaceId, pageable);
        }

        setContacts(data.content || []);
      } catch (error) {
        console.error("Failed to fetch recipients for page", error);
        const errorMessage = error.response?.data?.message || error.message || '페이지 데이터 로딩에 실패했습니다.';
        alert(errorMessage);
        setContacts([]);
      }
    }
  };

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
        // 새 연락처 추가 후 첫 페이지로 이동
        const currentPageInfo = getCurrentPageInfo();
        updatePageInfo(1, currentPageInfo.totalPages, currentPageInfo.totalElements);
        fetchRecipients(); // Refetch the list
      } catch (error) {
        console.error("Failed to create recipient", error);
        const errorMessage = error.response?.data?.message || error.message || '수신자 추가에 실패했습니다.';
        alert(errorMessage);
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
        const errorMessage = error.response?.data?.message || error.message || '수신자 삭제에 실패했습니다.';
        alert(errorMessage);
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

  const handleSelectGroup = async (group) => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');

    try {
      // 백엔드 API로 주소록에 수신자 추가 (단일)
      await phoneBookService.addRecipientsToPhoneBook(workspaceId, group.id, [editingContactId]);

      handleCloseModal();
      alert('연락처가 그룹에 연결되었습니다.');

      // 해당 주소록으로 자동 이동
      if (onGroupSelect) {
        onGroupSelect(group);
      }
    } catch (error) {
      console.error('그룹 연결 실패:', error);
      const errorMessage = error.response?.data?.message || error.message || '그룹 연결에 실패했습니다.';
      alert(errorMessage);
    }
  };

  // 다중 선택 핸들러
  const handleSelectContact = (contactId, checked) => {
    const newSelected = new Set(selectedContacts);
    if (checked) {
      newSelected.add(contactId);
    } else {
      newSelected.delete(contactId);
    }
    setSelectedContacts(newSelected);
    setSelectAll(newSelected.size === contacts.length);
  };

  const handleSelectAllContacts = (checked) => {
    if (checked) {
      const allIds = new Set(contacts.map(c => c.recipientId || c.id));
      setSelectedContacts(allIds);
    } else {
      setSelectedContacts(new Set());
    }
    setSelectAll(checked);
  };

  // 선택된 연락처들에 대해 일괄 그룹 연결
  const handleBulkGroupConnection = async (group) => {
    if (selectedContacts.size === 0) {
      alert('연결할 연락처를 선택해주세요.');
      return;
    }

    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    const selectedContactsArray = Array.from(selectedContacts);

    try {
      // 백엔드 API로 주소록에 수신자들 일괄 추가
      await phoneBookService.addRecipientsToPhoneBook(workspaceId, group.id, selectedContactsArray);

      setSelectedContacts(new Set()); // 선택 초기화
      setSelectAll(false);
      handleCloseModal();
      alert(`${selectedContactsArray.length}개의 연락처가 그룹에 연결되었습니다.`);

      // 해당 주소록으로 자동 이동
      if (onGroupSelect) {
        onGroupSelect(group);
      }
    } catch (error) {
      console.error('그룹 연결 실패:', error);
      const errorMessage = error.response?.data?.message || error.message || '그룹 연결에 실패했습니다.';
      alert(errorMessage);
    }
  };

  // 선택된 연락처들에 대해 일괄 삭제 (주소록에서 제거)
  const handleBulkDelete = async () => {
    if (selectedContacts.size === 0) {
      alert('삭제할 연락처를 선택해주세요.');
      return;
    }

    if (!window.confirm(`선택된 ${selectedContacts.size}개의 연락처를 이 주소록에서 삭제하시겠습니까?`)) {
      return;
    }

    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    const selectedContactsArray = Array.from(selectedContacts);

    try {
      // 백엔드 API로 주소록에서 수신자들 일괄 삭제
      await phoneBookService.deleteRecipientsFromPhoneBook(workspaceId, selectedGroup.id, selectedContactsArray);

      setSelectedContacts(new Set()); // 선택 초기화
      setSelectAll(false);
      fetchRecipients(); // 리스트 새로고침
      alert(`${selectedContactsArray.length}개의 연락처가 삭제되었습니다.`);
    } catch (error) {
      console.error('일괄 삭제 실패:', error);
      const errorMessage = error.response?.data?.message || error.message || '일괄 삭제에 실패했습니다.';
      alert(errorMessage);
    }
  };

  // 수정 모달 관련 핸들러
  const handleOpenEditModal = (contact) => {
    setEditContactData({
      recipientName: contact.recipientName || contact.name || '',
      recipientPhoneNumber: contact.recipientPhoneNumber || contact.phone || '',
      recipientMemo: contact.recipientMemo || contact.memo || ''
    });
    setEditingContactId(contact.recipientId || contact.id);
    setIsEditModalOpen(true);
  };

  const handleCloseEditModal = () => {
    setIsEditModalOpen(false);
    setEditContactData({
      recipientName: '',
      recipientPhoneNumber: '',
      recipientMemo: ''
    });
    setEditingContactId(null);
  };

  const handleUpdateContact = async () => {
    const workspaceId = localStorage.getItem('selectedWorkspaceId');
    if (workspaceId && editingContactId) {
      try {
        // 백엔드 DTO 형식에 맞게 데이터 변환
        const updateData = {
          newRecipientName: editContactData.recipientName,
          newRecipientPhoneNumber: editContactData.recipientPhoneNumber,
          newRecipientMemo: editContactData.recipientMemo || ''
        };

        await recipientService.updateRecipient(workspaceId, editingContactId, updateData);
        fetchRecipients(); // 목록 새로고침
        handleCloseEditModal();
        alert('연락처가 수정되었습니다.');
      } catch (error) {
        console.error('Failed to update recipient:', error);
        const errorMessage = error.response?.data?.message || error.message || '연락처 수정에 실패했습니다.';
        alert(errorMessage);
      }
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Box sx={{ flexShrink: 0, mb: 2 }}>
        <Typography variant="h6" sx={{ mb: 1.5, fontSize: '1.1rem' }}>연락처 입력 영역</Typography>
        <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
          <TextField
            label="수신인 이름"
            variant="outlined"
            size="small"
            value={newContactName}
            onChange={(e) => setNewContactName(e.target.value)}
            sx={{ flex: 1 }}
          />
          <TextField
            label="전화번호"
            variant="outlined"
            size="small"
            value={newContactPhone}
            onChange={(e) => setNewContactPhone(e.target.value)}
            sx={{ flex: 1 }}
          />
        </Box>
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
          <TextField
            label="메모"
            variant="outlined"
            size="small"
            value={newContactMemo}
            onChange={(e) => setNewContactMemo(e.target.value)}
            sx={{ flex: 1 }}
          />
          <Button variant="contained" onClick={handleAddContact} size="small">
            추가
          </Button>
        </Box>
      </Box>

      <Typography variant="h6" sx={{ mb: 1.5, fontSize: '1.1rem', flexShrink: 0 }}>
        {selectedGroup ? `연락처 리스트 - ${selectedGroup.name} (총 ${getCurrentPageInfo().totalElements}명)` : `연락처 리스트 - 전체 (총 ${getCurrentPageInfo().totalElements}명)`}
      </Typography>

      {selectedContacts.size > 0 && (
        <Box sx={{ mb: 2, p: 2, bgcolor: 'primary.light', borderRadius: '4px' }}>
          <Typography variant="body2" sx={{ mb: 1 }}>
            {selectedContacts.size}개의 연락처가 선택되었습니다.
          </Typography>
          {selectedGroup ? (
            <>
              <Button
                variant="contained"
                size="small"
                color="error"
                onClick={handleBulkDelete}
                sx={{ mr: 1 }}
              >
                선택된 연락처 삭제
              </Button>
              <Typography variant="caption" sx={{ display: 'block', mt: 1, color: 'text.secondary' }}>
                * 이 주소록에서만 제거됩니다 (수신자 자체는 삭제되지 않음)
              </Typography>
            </>
          ) : (
            <>
              <Button
                variant="contained"
                size="small"
                onClick={() => handleOpenModal('bulk')}
                sx={{ mr: 1 }}
              >
                선택된 연락처 그룹 연결
              </Button>
              <Typography variant="caption" sx={{ display: 'block', mt: 1, color: 'text.secondary' }}>
                * 전체 보기에서는 그룹 연결만 가능합니다
              </Typography>
            </>
          )}
          <Button
            variant="outlined"
            size="small"
            onClick={() => {
              setSelectedContacts(new Set());
              setSelectAll(false);
            }}
          >
            선택 해제
          </Button>
        </Box>
      )}

      <Box sx={{ flexGrow: 1, overflow: 'auto', border: '1px solid #eee', borderRadius: '4px' }}>
        <Table stickyHeader>
        <TableHead>
          <TableRow>
            <TableCell>
              <Checkbox
                checked={selectAll}
                indeterminate={selectedContacts.size > 0 && selectedContacts.size < contacts.length}
                onChange={(e) => handleSelectAllContacts(e.target.checked)}
              />
            </TableCell>
            <TableCell>수신인 이름</TableCell>
            <TableCell>수신인 전화번호</TableCell>
            <TableCell>메모</TableCell>
            <TableCell align="right">관리</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {contacts.map((contact) => {
            const contactId = contact.recipientId || contact.id;
            return (
              <TableRow key={contactId}>
                <TableCell>
                  <Checkbox
                    checked={selectedContacts.has(contactId)}
                    onChange={(e) => handleSelectContact(contactId, e.target.checked)}
                  />
                </TableCell>
                <TableCell>{contact.recipientName || contact.name}</TableCell>
                <TableCell>{contact.recipientPhoneNumber || contact.phone}</TableCell>
                <TableCell>{contact.recipientMemo || contact.memo || '-'}</TableCell>
                <TableCell align="right">
                  {!selectedGroup && (
                    <Button size="small" onClick={() => handleOpenModal(contactId)}>그룹 연결</Button>
                  )}
                  <IconButton aria-label="edit" onClick={() => handleOpenEditModal(contact)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteContact(contactId)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            );
          })}
        </TableBody>
        </Table>
      </Box>

      {(getCurrentPageInfo().totalPages > 1 || getCurrentPageInfo().totalElements > itemsPerPage) && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination
            count={Math.max(1, getCurrentPageInfo().totalPages)}
            page={getCurrentPageInfo().currentPage}
            onChange={handlePageChange}
            color="primary"
          />
        </Box>
      )}

      <PhoneBookModal
        open={isModalOpen}
        onClose={handleCloseModal}
        onSelect={editingContactId === 'bulk' ? handleBulkGroupConnection : handleSelectGroup}
      />

      {/* 연락처 수정 모달 */}
      <Dialog open={isEditModalOpen} onClose={handleCloseEditModal} maxWidth="sm" fullWidth>
        <DialogTitle>연락처 수정</DialogTitle>
        <DialogContent>
          <TextField
            label="수신인 이름"
            variant="outlined"
            size="small"
            fullWidth
            value={editContactData.recipientName}
            onChange={(e) => setEditContactData({...editContactData, recipientName: e.target.value})}
            sx={{ mb: 2, mt: 1 }}
          />
          <TextField
            label="수신인 전화번호"
            variant="outlined"
            size="small"
            fullWidth
            value={editContactData.recipientPhoneNumber}
            onChange={(e) => setEditContactData({...editContactData, recipientPhoneNumber: e.target.value})}
            sx={{ mb: 2 }}
          />
          <TextField
            label="수신인 메모"
            variant="outlined"
            size="small"
            fullWidth
            multiline
            rows={3}
            value={editContactData.recipientMemo}
            onChange={(e) => setEditContactData({...editContactData, recipientMemo: e.target.value})}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEditModal}>취소</Button>
          <Button onClick={handleUpdateContact} variant="contained">저장</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
