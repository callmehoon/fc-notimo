import React, { useState } from 'react';
import { Paper, InputBase, IconButton } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

// onSearch 라는 함수를 props로 받습니다.
export default function SearchInput({ onSearch }) {
    const [searchTerm, setSearchTerm] = useState('');

    const handleSearch = () => {
        // 부모로부터 받은 onSearch 함수를 호출하여 현재 검색어를 전달합니다.
        onSearch(searchTerm);
    };

    const handleKeyPress = (event) => {
        // Enter 키를 눌렀을 때도 검색이 실행되도록 합니다.
        if (event.key === 'Enter') {
            handleSearch();
        }
    };

    return (
        <Paper
            component="form"
            onSubmit={(e) => { e.preventDefault(); handleSearch(); }} // form 제출 시 검색 실행
            sx={{ p: '2px 4px', display: 'flex', alignItems: 'center', width: 400 }}
        >
            <InputBase
                sx={{ ml: 1, flex: 1 }}
                placeholder="템플릿 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={handleKeyPress}
            />
            <IconButton type="submit" sx={{ p: '10px' }}>
                <SearchIcon />
            </IconButton>
        </Paper>
    );
}