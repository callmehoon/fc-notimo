// src/pages/FavoriteTemplatesPage.js
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
    Box, CssBaseline, FormControl, Select, MenuItem
} from '@mui/material';

import Sidebar from '../components/layout/Sidebar';
import WorkspaceList from '../components/layout/WorkspaceList';
import SearchInput from '../components/common/SearchInput';
import Pagination from '../components/common/Pagination';
import TemplateCard from '../components/template/TemplateCard';
import { getFavoriteTemplates } from '../services/favoriteService';

const ITEMS_PER_PAGE = 12;

export default function FavoriteTemplatesPage() {
    const params = useParams();
    // 라우터에 :workspaceId가 없으면 localStorage에서 보조
    const workspaceId = params.workspaceId ?? localStorage.getItem('selectedWorkspaceId');

    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [sortOrder, setSortOrder] = useState('최신 순');
    const [templates, setTemplates] = useState([]);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        const fetchFavorites = async () => {
            if (!workspaceId) return;

            try {
                const pageable = {
                    page: currentPage - 1, // 0-based
                    size: ITEMS_PER_PAGE,
                    // 백엔드 정렬 파라미터가 있으면 여기서 추가
                    // sortType/direction 등을 쓰는 구조라면 favoriteService에서 params로 넘겨도 됨
                };

                // templateType이 필요하면 'PUBLIC' or 'INDIVIDUAL' 등으로 지정
                const res = await getFavoriteTemplates(workspaceId, null, pageable);

                // 응답 형태에 따라 맞추기
                const data = res.data;
                setTemplates(data.content ?? []);
                if (typeof data.totalPages === 'number') {
                    setTotalPages(data.totalPages);
                } else if (typeof data.totalElements === 'number') {
                    setTotalPages(Math.ceil(data.totalElements / ITEMS_PER_PAGE));
                } else {
                    setTotalPages(1);
                }
            } catch (error) {
                console.error('Error fetching favorite templates:', error);
            }
        };

        fetchFavorites();
    }, [workspaceId, currentPage, sortOrder]);

    const handleSearch = (q) => { setSearchQuery(q); setCurrentPage(1); };
    const handlePageChange = (_, v) => setCurrentPage(v);
    const handleSortChange = (e) => setSortOrder(e.target.value);

    // 템플릿 제목이 null일 수도 있으니 안전 처리
    const filteredTemplates = templates.filter(t =>
        (t.templateTitle ?? '').toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
            <CssBaseline />
            <Sidebar>
                <WorkspaceList />
            </Sidebar>

            <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexShrink: 0 }}>
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

                <Box sx={{ width: '100%', flexGrow: 1, overflow: 'auto', p: 1 }}>
                    <Box 
                        sx={{ 
                            display: 'grid',
                            gridTemplateColumns: 'repeat(4, 1fr)',
                            gap: 2,
                            '@media (max-width: 1200px)': {
                                gridTemplateColumns: 'repeat(3, 1fr)',
                            },
                            '@media (max-width: 900px)': {
                                gridTemplateColumns: 'repeat(2, 1fr)',
                            },
                            '@media (max-width: 600px)': {
                                gridTemplateColumns: '1fr',
                            },
                        }}
                    >
                        {filteredTemplates.map(template => (
                            <TemplateCard 
                                key={template.favoriteId ?? `${template.templateId}-fav`} 
                                template={{
                                    id: template.templateId,
                                    title: template.templateTitle,
                                    content: template.templateContent,
                                }} 
                            />
                        ))}
                    </Box>
                </Box>

                <Pagination count={totalPages} page={currentPage} onChange={handlePageChange} />
            </Box>
        </Box>
    );
}
