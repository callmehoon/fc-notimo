import * as React from 'react';
import { Drawer, Box, List, ListItem, ListItemButton, ListItemText } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import WorkspaceList from './WorkspaceList';
import workspaceService from '../../services/workspaceService'; // Import workspaceService

const sidebarWidth = 240;
const menuItems = ['공용 템플릿', '나의 템플릿', '즐겨찾기', '회원정보 수정', '연락처 관리', '워크스페이스 관리'];
const routeMap = {
    '공용 템플릿': '/publicTemplate',
    '나의 템플릿': '/mytemplate',
    '즐겨찾기': '/favoritetemplates',
    '회원정보 수정': '/profile-edit',
    '연락처 관리': '/contact-management',
    '워크스페이스 관리': '/workspace-edit/:id',
};

export default function Sidebar() {
    const navigate = useNavigate();
    const location = useLocation();
    const [allWorkspaces, setAllWorkspaces] = React.useState([]);
    const [selectedWorkspace, setSelectedWorkspace] = React.useState(null);
    const [open, setOpen] = React.useState(false);

    React.useEffect(() => {
        const fetchWorkspaces = async () => {
            try {
                const workspaces = await workspaceService.getWorkspaces();
                setAllWorkspaces(workspaces);

                if (workspaces.length > 0) {
                    const storedWorkspaceId = localStorage.getItem('selectedWorkspaceId');
                    const workspaceToSelect = workspaces.find(ws => ws && ws.workspaceId && ws.workspaceId.toString() === storedWorkspaceId) || workspaces[0];
                    setSelectedWorkspace(workspaceToSelect);
                    localStorage.setItem('selectedWorkspaceId', workspaceToSelect.workspaceId.toString());
                }
            } catch (error) {
                console.error("Failed to fetch workspaces:", error);
                // Handle error appropriately
            }
        };

        fetchWorkspaces();
    }, []);

    const currentPath = location.pathname;
    const selectedIndex = React.useMemo(() => {
        const currentRoute = Object.values(routeMap).find(route => currentPath.startsWith(route));
        if (currentRoute) {
            const menuItemText = Object.keys(routeMap).find(key => routeMap[key] === currentRoute);
            return menuItems.indexOf(menuItemText);
        }
        return -1;
    }, [currentPath]);

    const handleMenuItemClick = (text) => {
        let path = routeMap[text];
        if (path) {
            if (text === '워크스페이스 관리' && selectedWorkspace) {
                path = path.replace(':id', selectedWorkspace.workspaceId);
            } else if (text === '워크스페이스 관리' && !selectedWorkspace) {
                alert('워크스페이스를 선택해주세요.');
                return;
            }
            navigate(path);
        }
    };

    const handleSelectWorkspace = (workspace) => {
        setSelectedWorkspace(workspace);
        localStorage.setItem('selectedWorkspaceId', workspace.workspaceId.toString());
        setOpen(false);
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
                                onClick={() => handleMenuItemClick(text)}
                                sx={{
                                    bgcolor: '#e9ecef',
                                    border: '1px solid #ccc',
                                    borderRadius: '4px',
                                    '&.Mui-selected': {
                                        bgcolor: 'white',
                                        border: '1px solid #333',
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

                {selectedWorkspace && (
                    <WorkspaceList
                        allWorkspaces={allWorkspaces}
                        selectedWorkspace={selectedWorkspace}
                        open={open}
                        onToggle={() => setOpen(!open)}
                        onSelect={handleSelectWorkspace}
                    />
                )}
            </Box>
        </Drawer>
    );
}
