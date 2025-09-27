import * as React from 'react';
import {Box, List, ListItem, ListItemButton, ListItemText, ListItemIcon, Typography} from '@mui/material';
import {
    Public as PublicIcon,
    Description as DescriptionIcon,
    Favorite as FavoriteIcon,
    Person as PersonIcon,
    Contacts as ContactsIcon,
    Business as BusinessIcon
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import WorkspaceList from './WorkspaceList';
import workspaceService from '../../services/workspaceService';
const menuItems = [
    { text: '공용 템플릿', icon: <PublicIcon />, path: '/publicTemplate' },
    { text: '나의 템플릿', icon: <DescriptionIcon />, path: '/mytemplate' },
    { text: '즐겨찾기', icon: <FavoriteIcon />, path: '/favoritetemplates' },
    { text: '회원정보 수정', icon: <PersonIcon />, path: '/profile-edit' },
    { text: '연락처 관리', icon: <ContactsIcon />, path: '/contact-management' },
    { text: '워크스페이스 관리', icon: <BusinessIcon />, path: '/workspace-edit/:id' },
];

export default function Sidebar() {
    const navigate = useNavigate();
    const location = useLocation();
    const [allWorkspaces, setAllWorkspaces] = React.useState([]);
    const [selectedWorkspace, setSelectedWorkspace] = React.useState(null);
    const [open, setOpen] = React.useState(false);

    React.useEffect(() => {
        const fetchWorkspaces = async () => {
            try {
                console.log('워크스페이스 데이터 로딩 시작...');
                const workspaces = await workspaceService.getWorkspaces();
                console.log('워크스페이스 데이터 로딩 완료:', workspaces);

                setAllWorkspaces(workspaces || []);

                if (workspaces && workspaces.length > 0) {
                    const storedWorkspaceId = localStorage.getItem('selectedWorkspaceId');
                    const workspaceToSelect = workspaces.find(ws => ws && ws.workspaceId && ws.workspaceId.toString() === storedWorkspaceId) || workspaces[0];
                    console.log('선택된 워크스페이스:', workspaceToSelect);
                    setSelectedWorkspace(workspaceToSelect);
                    localStorage.setItem('selectedWorkspaceId', workspaceToSelect.workspaceId.toString());
                } else {
                    console.log('워크스페이스 데이터가 없습니다.');
                }
            } catch (error) {
                console.error("워크스페이스 로딩 실패:", error);
                setAllWorkspaces([]);
                setSelectedWorkspace(null);
            }
        };

        fetchWorkspaces();
    }, []);

    const currentPath = location.pathname;
    const selectedIndex = React.useMemo(() => {
        const currentRoute = menuItems.find(item => currentPath.startsWith(item.path));
        if (currentRoute) {
            return menuItems.indexOf(currentRoute);
        }
        return -1;
    }, [currentPath]);

    const handleMenuItemClick = (item) => {
        let path = item.path;
        if (path) {
            if (item.text === '워크스페이스 관리' && selectedWorkspace) {
                path = path.replace(':id', selectedWorkspace.workspaceId);
            } else if (item.text === '워크스페이스 관리' && !selectedWorkspace) {
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
        <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
            {/* 메뉴 영역 */}
            <List sx={{ flexGrow: 1, pt: 0 }}>
                {menuItems.map((item, index) => (
                    <ListItem key={item.text} disablePadding>
                        <ListItemButton
                            selected={selectedIndex === index}
                            onClick={() => handleMenuItemClick(item)}
                            sx={{
                                minHeight: 48,
                                '&.Mui-selected': {
                                    backgroundColor: 'rgba(25, 118, 210, 0.12)',
                                    '&:hover': {
                                        backgroundColor: 'rgba(25, 118, 210, 0.18)',
                                    },
                                },
                                '&:hover': {
                                    backgroundColor: 'rgba(0, 0, 0, 0.04)',
                                },
                            }}
                        >
                            <ListItemIcon
                                sx={{
                                    minWidth: 40,
                                    color: selectedIndex === index ? 'primary.main' : 'inherit',
                                }}
                            >
                                {item.icon}
                            </ListItemIcon>
                            <ListItemText
                                primary={item.text}
                                primaryTypographyProps={{
                                    fontSize: '0.875rem',
                                    fontWeight: selectedIndex === index ? 600 : 400,
                                    color: selectedIndex === index ? 'primary.main' : 'inherit',
                                }}
                            />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>

            {/* 워크스페이스 영역 - 항상 표시 */}
            <Box sx={{
                mt: 2,
                px: 1,
                borderTop: '1px solid #e0e0e0',
                pt: 2
            }}>
                {selectedWorkspace ? (
                    <WorkspaceList
                        allWorkspaces={allWorkspaces}
                        selectedWorkspace={selectedWorkspace}
                        open={open}
                        onToggle={() => setOpen(!open)}
                        onSelect={handleSelectWorkspace}
                    />
                ) : allWorkspaces.length > 0 ? (
                    <Box sx={{ textAlign: 'center', p: 2 }}>
                        <Typography variant="body2" color="text.secondary">
                            워크스페이스 선택 중...
                        </Typography>
                    </Box>
                ) : (
                    <Box sx={{ textAlign: 'center', p: 2 }}>
                        <Typography variant="body2" color="text.secondary">
                            워크스페이스 로딩 중...
                        </Typography>
                    </Box>
                )}
            </Box>
        </Box>
    );
}
