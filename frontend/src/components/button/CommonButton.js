import React from 'react';
import { Button } from '@mui/material';
import PropTypes from 'prop-types';

/**
 * MUI Button을 기반으로 한 공통 버튼 컴포넌트
 * @param {node} children - 버튼 내에 표시될 내용 (주로 텍스트)
 * @param {func} onClick - 클릭 이벤트 핸들러
 * @param {string} variant - 버튼 스타일 ('contained', 'outlined', 'text')
 * @param {string} color - 버튼 색상 ('primary', 'secondary', 'success', 'error', etc.)
 * @param {bool} fullWidth - true일 경우 부모 요소의 전체 너비를 차지
 * @param {object} sx - MUI sx prop을 이용한 커스텀 스타일
 */

const CommonButton = ({ children, onClick, variant, color, fullWidth, sx, ...props }) => {
    return (
        <Button
            variant={variant}
            color={color}
            fullWidth={fullWidth}
            onClick={onClick}
            sx={{
                py: 1.5, // padding-top, padding-bottom
                fontWeight: 'bold',
                ...sx, // 추가적인 커스텀 스타일 적용
            }}
            {...props} // type="submit" 등의 추가 속성을 받을 수 있도록 함
        >
            {children}
        </Button>
    );
};

CommonButton.propTypes = {
    children: PropTypes.node.isRequired,
    onClick: PropTypes.func,
    variant: PropTypes.oneOf(['contained', 'outlined', 'text']),
    color: PropTypes.oneOf(['inherit', 'primary', 'secondary', 'success', 'error', 'info', 'warning']),
    fullWidth: PropTypes.bool,
    sx: PropTypes.object,
};

CommonButton.defaultProps = {
    onClick: () => {},
    variant: 'contained',
    color: 'primary',
    fullWidth: false,
    sx: {},
};

export default CommonButton;