import React from 'react';
import { Container, Box, Typography, Avatar } from '@mui/material';
import PropTypes from 'prop-types';
import logo from '../../assets/logo.png';

/**
 * 로그인, 회원가입 등 폼을 위한 공통 레이아웃
 * @param {string} title - 폼 상단에 표시될 제목
 * @param {node} children - 폼 내용을 구성하는 자식 요소들
 */
const FormLayout = ({ title, children,  onSubmit }) => {
    return (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Box
                    sx={{
                        width: '150px',
                        height: '80px',
                    }}
                >
                    <img src={logo} alt="Logo" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                </Box>
                <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
                    {title}
                </Typography>
                <Box component="form" onSubmit={onSubmit} sx={{ mt: 1 }}>
                    {children}
                </Box>
            </Box>
        </Container>
    );
};

FormLayout.propTypes = {
    title: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired,
    onSubmit: PropTypes.func.isRequired,
};

export default FormLayout;