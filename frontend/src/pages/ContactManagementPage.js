import React, { useState } from 'react';
import { Box, CssBaseline, Typography } from '@mui/material';

// --- 공용 컴포넌트 임포트 ---
import Sidebar from '../components/layout/Sidebar';
import WorkspaceList from '../components/layout/WorkspaceList';

// --- 연락처 관리 컴포넌트 임포트 ---
import AddressBookManager from '../components/ContactManagement/AddressBookManager';
import ContactListManager from '../components/ContactManagement/ContactListManager';

export default function ContactManagementPage() {
  const [selectedGroup, setSelectedGroup] = useState(null);

  const handleGroupSelect = (group) => {
    setSelectedGroup(group);
  };

  return (
    <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <CssBaseline />

      <Sidebar>
        <WorkspaceList />
      </Sidebar>

      <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
        <Typography variant="h5" component="h1" sx={{ mb: 3, flexShrink: 0 }}>연락처 관리</Typography>

        <Box sx={{ flexGrow: 1, display: 'flex', gap: 3, overflow: 'hidden' }}>
          {/* 중앙 영역: 주소록 관리 */}
          <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', border: '1px solid #ddd', borderRadius: '8px', p: 2, overflow: 'hidden' }}>
            <AddressBookManager onGroupSelect={handleGroupSelect} selectedGroup={selectedGroup} />
          </Box>

          {/* 우측 영역: 연락처 목록 */}
          <Box sx={{ flex: 2, display: 'flex', flexDirection: 'column', border: '1px solid #ddd', borderRadius: '8px', p: 2, overflow: 'hidden' }}>
            <ContactListManager selectedGroup={selectedGroup} onGroupSelect={handleGroupSelect} />
          </Box>
        </Box>
      </Box>
    </Box>
  );
}
