import React from 'react';
import { Box, Container } from '@mui/material';

/**
 * 로그인 후 사용될 메인 콘텐츠 영역을 위한 레이아웃 컴포넌트입니다.
 * FormLayout보다 넓은 너비를 가집니다.
 * @param {object} props
 * @param {React.ReactNode} props.children - 이 레이아웃 안에 렌더링될 자식 요소들
 */
const MainLayout = ({ children }) => {
    return (
        // 'xl' 너비의 컨테이너를 사용하여 FormLayout보다 넓은 영역을 확보합니다.
        <Container component="main" maxWidth="xl" sx={{ mt: 8, mb: 4 }}>
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    width: '100%',
                }}
            >
                {children}
            </Box>
        </Container>
    );
};

export default MainLayout;