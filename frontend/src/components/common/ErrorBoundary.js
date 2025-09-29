import React from 'react';
import { Box, Typography, Button } from '@mui/material';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null, errorInfo: null };
    }

    static getDerivedStateFromError(error) {
        // 다음 렌더링에서 폴백 UI가 보이도록 상태를 업데이트합니다.
        return { hasError: true };
    }

    componentDidCatch(error, errorInfo) {
        // 에러 리포팅 서비스에 에러를 기록할 수도 있습니다
        console.error('ErrorBoundary caught an error:', error, errorInfo);
        this.setState({
            error: error,
            errorInfo: errorInfo
        });
    }

    handleReset = () => {
        this.setState({ hasError: false, error: null, errorInfo: null });
    };

    render() {
        if (this.state.hasError) {
            // 폴백 UI를 커스터마이징할 수 있습니다
            return (
                <Box
                    sx={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        height: '50vh',
                        gap: 2,
                        p: 3
                    }}
                >
                    <Typography variant="h5" color="error">
                        문제가 발생했습니다
                    </Typography>
                    <Typography variant="body1" color="textSecondary" align="center">
                        페이지를 불러오는 중 오류가 발생했습니다.
                        페이지를 새로고침하거나 다시 시도해주세요.
                    </Typography>
                    <Button
                        variant="contained"
                        onClick={this.handleReset}
                        sx={{ mt: 2 }}
                    >
                        다시 시도
                    </Button>
                    {process.env.NODE_ENV === 'development' && (
                        <Box sx={{ mt: 2, p: 2, bgcolor: '#f5f5f5', borderRadius: 1, maxWidth: '80%' }}>
                            <Typography variant="caption" component="pre" sx={{ whiteSpace: 'pre-wrap' }}>
                                {this.state.error && this.state.error.toString()}
                                {this.state.errorInfo.componentStack}
                            </Typography>
                        </Box>
                    )}
                </Box>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;