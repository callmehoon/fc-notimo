import React, { useState, useMemo } from 'react';
import {
    Box,
    CssBaseline,
    Grid,
    Typography, // Added for title
    FormControl, // Added for sort dropdown structure
    Select, // Added for sort dropdown structure
    MenuItem // Added for sort dropdown structure
} from '@mui/material';

// --- 아이콘 및 공용 컴포넌트 임포트 ---
import Sidebar from '../components/layout/Sidebar';
import WorkspaceList from '../components/layout/WorkspaceList';
import SearchInput from '../components/common/SearchInput';
import Pagination from '../components/common/Pagination';
import TemplateCard from '../components/template/TemplateCard';
// CommonButton은 이 페이지에서 직접 사용하지 않을 수 있으나, PublicTemplatePage와 유사하게 구성

// --- 목업 데이터 (즐겨찾기 템플릿) ---
const mockFavoriteTemplates = Array.from({ length: 10 }, (_, i) => ({
    id: `fav-${i + 1}`,
    title: `즐겨찾기 템플릿 ${i + 1}`,
    content: '이것은 즐겨찾기된 템플릿 내용입니다.',
    status: ['활성', '비활성'][i % 2], // 예시 상태
}));

const ITEMS_PER_PAGE = 12; // PublicTemplatePage와 동일하게 설정

export default function FavoriteTemplatesPage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [sortOrder, setSortOrder] = useState('최신 순');

    // PublicTemplatePage와 유사한 핸들러 (기능은 목업)
    const handleSearch = (query) => { setSearchQuery(query); setCurrentPage(1); };
    const handlePageChange = (event, value) => { setCurrentPage(value); };
    const handleSortChange = (event) => { setSortOrder(event.target.value); };

    const finalFilteredTemplates = useMemo(() => {
        let templates = mockFavoriteTemplates;
        if (searchQuery) {
            templates = templates.filter(t => t.title.toLowerCase().includes(searchQuery.toLowerCase()));
        }
        return templates;
    }, [searchQuery]);

    const totalPages = Math.ceil(finalFilteredTemplates.length / ITEMS_PER_PAGE);
    const paginatedTemplates = finalFilteredTemplates.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

    return (
        <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
            <CssBaseline />

            <Sidebar>
                <WorkspaceList />
            </Sidebar>

            <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexShrink: 0 }}>
                    {/* 페이지 제목 제거됨 */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <SearchInput onSearch={handleSearch} />
                        <FormControl size="small" sx={{ minWidth: 120 }}>
                            <Select value={sortOrder} onChange={handleSortChange}>
                                <MenuItem value={'최신 순'}>최신 순</MenuItem>
                                <MenuItem value={'인기 순'}>인기 순</MenuItem>
                            </Select>
                        </FormControl>
                    </Box>
                </Box>

                <Box sx={{ width: '100%', flexGrow: 1, overflow: 'auto', p: 0.5 }}>
                    <Box
                        sx={{
                            display: 'flex',
                            flexWrap: 'wrap',
                        }}
                    >
                        {paginatedTemplates.map(template => (
                            <Box
                                key={template.id}
                                sx={{
                                    flex: '0 0 25%',    // 한 줄에 4개
                                    boxSizing: 'border-box',
                                    p: 1,               // 카드 사이 여백
                                }}
                            >
                                <TemplateCard template={template} />
                            </Box>
                        ))}
                    </Box>
                </Box>


                <Pagination
                    count={totalPages}
                    page={currentPage}
                    onChange={handlePageChange}
                />
            </Box>
        </Box>
    );
}
