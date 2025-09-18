import React from 'react';
import { Pagination as MuiPagination, Box } from '@mui/material';

// count: 전체 페이지 수
// page: 현재 페이지 번호
// onChange: 페이지 변경 시 호출될 함수
export default function Pagination({ count, page, onChange }) {
    return (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <MuiPagination
                count={count}
                page={page}
                onChange={onChange}
                color="primary"
            />
        </Box>
    );
}