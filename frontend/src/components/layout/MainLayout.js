import React, { useState } from 'react';
import {
    Box,
    Drawer,
    AppBar,
    Toolbar,
    Typography,
    Divider,
    IconButton,
    Avatar,
    Menu,
    MenuItem,
    useTheme,
    useMediaQuery,
    ListItemIcon
} from '@mui/material';
import {
    Settings as SettingsIcon,
    Logout as LogoutIcon,
    AccountCircle as AccountCircleIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import logo from '../../assets/logo.png';
import Sidebar from "./Sidebar";

const drawerWidth = 240;

/**
 * 로그인 후 사용될 메인 레이아웃 컴포넌트
 * Header, Sidebar, Content 영역으로 구성
 * 사이드바는 항상 열려있음
 */
// MainLayout 내부 컴포넌트
const MainLayoutContent = ({ children }) => {
    const [anchorEl, setAnchorEl] = useState(null);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const navigate = useNavigate();

    const handleMenu = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleProfile = () => {
        navigate('/profile-edit');
        handleClose();
    };

    const handleSettings = () => {
        alert('준비중입니다.');
        handleClose();
    };

    const handleLogout = () => {
        // 로그아웃 로직 구현
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userRole');
        navigate('/login');
        handleClose();
    };

    const drawer = (
        <div>
            <Toolbar
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    px: [1],
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <img src={logo} alt="Logo" style={{ width: '150px', height: '80px', objectFit: 'cover' }} />
                </Box>
            </Toolbar>
            <Divider />
            <Sidebar />
        </div>
    );

    return (
        <Box sx={{ display: 'flex'}}>
            {/* Header */}
            <AppBar
                position="fixed"
                sx={{
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                }}
            >
                <Toolbar sx={{ minHeight: 0 }}>
                    <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
                        알림톡 관리 시스템
                    </Typography>
                    <div>
                        <IconButton
                            size="large"
                            aria-label="account of current user"
                            aria-controls="menu-appbar"
                            aria-haspopup="true"
                            onClick={handleMenu}
                            color="inherit"
                        >
                            <Avatar sx={{ width: 32, height: 32 }}>
                                <AccountCircleIcon />
                            </Avatar>
                        </IconButton>
                        <Menu
                            id="menu-appbar"
                            anchorEl={anchorEl}
                            anchorOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            keepMounted
                            transformOrigin={{
                                vertical: 'top',
                                horizontal: 'right',
                            }}
                            open={Boolean(anchorEl)}
                            onClose={handleClose}
                        >
                            <MenuItem onClick={handleProfile}>
                                <ListItemIcon>
                                    <AccountCircleIcon fontSize="small" />
                                </ListItemIcon>
                                프로필
                            </MenuItem>
                            <MenuItem onClick={handleSettings}>
                                <ListItemIcon>
                                    <SettingsIcon fontSize="small" />
                                </ListItemIcon>
                                설정
                            </MenuItem>
                            <Divider />
                            <MenuItem onClick={handleLogout}>
                                <ListItemIcon>
                                    <LogoutIcon fontSize="small" />
                                </ListItemIcon>
                                로그아웃
                            </MenuItem>
                        </Menu>
                    </div>
                </Toolbar>
            </AppBar>

            {/* Sidebar */}
            <Box
                component="nav"
                sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
            >
                <Drawer
                    variant="permanent"
                    sx={{
                        '& .MuiDrawer-paper': {
                            width: drawerWidth,
                            boxSizing: 'border-box',
                        },
                    }}
                    open
                >
                    {drawer}
                </Drawer>
            </Box>

            {/* Content */}
            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    bgcolor: 'background.default',
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                }}
            >
                <Toolbar />
                {children}
            </Box>
        </Box>
    );
};

// MainLayout 컴포넌트
const MainLayout = ({ children }) => {
    return <MainLayoutContent>{children}</MainLayoutContent>;
};

export default MainLayout;