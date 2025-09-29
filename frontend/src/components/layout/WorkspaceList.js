import * as React from 'react';
import { Box, Paper, Typography, IconButton } from '@mui/material';
import ArrowDropUpIcon from '@mui/icons-material/ArrowDropUp';
import ArrowDropDownIcon from '@mui/icons-material/ArrowDropDown';
import { useNavigate } from 'react-router-dom'; // Import useNavigate

export default function WorkspaceList({
                                         allWorkspaces,
                                         selectedWorkspace,
                                         open,
                                         onToggle,
                                         onSelect
                                     }) {
    const navigate = useNavigate();

    const otherWorkspaces = allWorkspaces.filter(ws => ws.workspaceId !== selectedWorkspace.workspaceId); // Filter by id

    const handleEditClick = (event, workspaceId) => {
        event.stopPropagation(); // Prevent onToggle/onSelect from firing
        navigate(`/workspace-edit/${workspaceId}`);
    };

    return (
        <Paper elevation={0} sx={{ width: '100%', border: '2px solid #333', borderRadius: '8px', overflow: 'hidden' }}>

            {/* 변경점 1: 선택된 워크스페이스를 가장 먼저 렌더링합니다. */}
            <Box
                onClick={onToggle}
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    p: '8px 0 8px 12px',
                    cursor: 'pointer',
                    backgroundColor: '#ffffff',
                    // 메뉴가 열렸을 때 아래쪽 테두리를 추가해 시각적으로 분리합니다.
                    borderBottom: open ? '2px solid #333' : 'none',
                }}
            >
                <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <Typography variant="body2" fontWeight="bold">{selectedWorkspace.workspaceName}</Typography> {/* Display name */}
                </Box>
                <Box sx={{ borderLeft: '2px solid #333', ml: 1, pl: 1, pr: 0.5, alignSelf: 'stretch', display: 'flex', alignItems: 'center' }}>
                    {open ? <ArrowDropUpIcon /> : <ArrowDropDownIcon />}
                </Box>
            </Box>

            {/* 변경점 2: 나머지 워크스페이스 목록을 선택된 항목 아래에 렌더링합니다. */}
            {open && otherWorkspaces.map((workspace, index) => (
                <Box
                    key={workspace.workspaceId} // Use id as key
                    onClick={() => onSelect(workspace)} // Pass full object
                    sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        p: '8px 12px',
                        cursor: 'pointer',
                        backgroundColor: '#f0f0f0',
                        // 마지막 항목이 아닐 경우에만 아래쪽 테두리를 추가합니다.
                        borderBottom: index < otherWorkspaces.length - 1 ? '2px solid #333' : 'none',
                        '&:hover': {
                            backgroundColor: '#e0e0e0',
                        },
                    }}
                >
                    <Typography variant="body2" fontWeight="bold">{workspace.workspaceName}</Typography> {/* Display name */}
                </Box>
            ))}
        </Paper>
    );
}
