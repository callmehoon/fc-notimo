import React from 'react';
import { TextField } from '@mui/material';
import PropTypes from 'prop-types';

/**
 * MUI TextField를 기반으로 한 공통 텍스트 입력 필드
 * @param {string} label - 입력 필드 라벨
 * @param {string} name - form에서 필드를 식별하기 위한 이름
 * @param {any} value - 입력 필드의 값
 * @param {func} onChange - 값이 변경될 때 호출되는 함수
 * @param {string} type - 입력 타입 (e.g., 'text', 'password', 'email')
 * @param {bool} required - 필수 입력 여부
 */
const CommonTextField = ({ label, name, value, onChange, type, required, error, helperText, ...props }) => {
    return (
        <TextField
            margin="none"
            required={required}
            fullWidth
            id={name}
            label={label}
            name={name}
            type={type}
            value={value}
            onChange={onChange}
            error={error}
            helperText={helperText}
            {...props} // autoFocus, autoComplete 등의 추가 속성을 받을 수 있도록 함
        />
    );
};

CommonTextField.propTypes = {
    label: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    value: PropTypes.any.isRequired,
    onChange: PropTypes.func.isRequired,
    type: PropTypes.string,
    required: PropTypes.bool,
    error: PropTypes.bool,
    helperText: PropTypes.string,
};

CommonTextField.defaultProps = {
    type: 'text',
    required: false,
    error: false,
    helperText: '',
};

export default CommonTextField;