import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import AppRouter from './router';
import './App.css';

// 1. MUI의 기본 테마를 생성
// 나중에 이 부분을 커스터마이징하여 프로젝트만의 색상, 폰트 등을 정의할 수 있습니다.
const theme = createTheme({
    palette: {
        // 예: primary: { main: '#1976d2' }
    },
});

function App() {
    return (
        // 2. ThemeProvider로 전체 앱을 감싸 테마를 적용합니다.
        <ThemeProvider theme={theme}>
            {/* 3. CssBaseline은 브라우저 간의 CSS 차이를 없애고 일관된 스타일링을 제공합니다. */}
            <CssBaseline />
            <Router>
                <AppRouter />
            </Router>
        </ThemeProvider>
    );
}

export default App;