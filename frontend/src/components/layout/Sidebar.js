import * as React from 'react';
import { Drawer, Box, List, ListItem, ListItemButton, ListItemText } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom'; // Import useNavigate and useLocation
import WorkspaceList from './WorkspaceList';

const sidebarWidth = 240;
const menuItems = ['공용 템플릿', '나의 템플릿', '즐겨찾기', '회원정보 수정', '연락처 관리', '워크스페이스 관리'];
const routeMap = { // Map menu items to their routes
    '공용 템플릿': '/publicTemplate',
    '나의 템플릿': '/mytemplate',
    '즐겨찾기': '/favoritetemplates',
    '회원정보 수정': '/profile-edit', // Added route for User Profile Edit
    '연락처 관리': '/contact-management', // Added route for Contact Management
    '워크스페이스 관리': '/workspace-edit/:id',
};

// 목업 데이터와 상태 관리를 Sidebar로 가져옵니다.
const allWorkspaces = [
    { id: '1', name: '워크스페이스 1' },
    { id: '2', name: '워크스페이스 2' },
    { id: '3', name: '워크스페이스 3' },
];

export default function Sidebar() { // Remove selectedIndex prop, will determine internally
    const navigate = useNavigate();
    const location = useLocation();
    const [selectedWorkspace, setSelectedWorkspace] = React.useState(allWorkspaces[0]);
    const [open, setOpen] = React.useState(false);

    // Determine selectedIndex based on current path
    const currentPath = location.pathname;
    const selectedIndex = React.useMemo(() => {
        const currentRoute = Object.values(routeMap).find(route => currentPath.startsWith(route));
        if (currentRoute) {
            const menuItemText = Object.keys(routeMap).find(key => routeMap[key] === currentRoute);
            return menuItems.indexOf(menuItemText);
        }
        return -1; // No item selected
    }, [currentPath]);


    const handleMenuItemClick = (text) => {
        const path = routeMap[text];
        if (path) {
            navigate(path);
        }
    };

    return (
        <Drawer
            variant="permanent"
            sx={{
                width: sidebarWidth,
                flexShrink: 0,
                '& .MuiDrawer-paper': {
                    width: sidebarWidth,
                    boxSizing: 'border-box',
                    bgcolor: '#f0f0f0',
                    borderRight: '1px solid #ddd'
                }
            }}
        >
            <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', height: '100%' }}>
                <List sx={{ flexGrow: 1 }}>
                    {menuItems.map((text, index) => (
                        <ListItem key={text} disablePadding sx={{ mb: 1 }}>
                            <ListItemButton
                                selected={selectedIndex === index}
                                onClick={() => handleMenuItemClick(text)} // Add onClick handler
                                sx={{
                                    bgcolor: '#e9ecef',
                                    border: '1px solid #ccc',
                                    borderRadius: '4px',
                                    '&.Mui-selected': {
                                        bgcolor: 'white', // 선택 시 배경색
                                        border: '1px solid #333', // 선택 시 테두리색
                                        '&:hover': { bgcolor: 'white' }
                                    },
                                    '&:hover': { bgcolor: '#dee2e6' }
                                }}
                            >
                                <ListItemText
                                    primary={text}
                                    primaryTypographyProps={{ fontWeight: selectedIndex === index ? 'bold' : 'normal' }}
                                />
                            </ListItemButton>
                        </ListItem>
                    ))}
                </List>

                <WorkspaceList
                    allWorkspaces={allWorkspaces}
                    selectedWorkspace={selectedWorkspace}
                    open={open}
                    onToggle={() => setOpen(!open)}
                    onSelect={(workspace) => {
                        setSelectedWorkspace(workspace);
                        setOpen(false);
                    }}
                />

            </Box>
        </Drawer>
    );
}
