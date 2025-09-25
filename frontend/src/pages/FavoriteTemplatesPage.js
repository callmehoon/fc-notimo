import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
    Box,
    CssBaseline,
    Grid,
    FormControl,
    Select,
    MenuItem
} from '@mui/material';

import Sidebar from '../components/layout/Sidebar';
import WorkspaceList from '../components/layout/WorkspaceList';
import SearchInput from '../components/common/SearchInput';
import Pagination from '../components/common/Pagination';
import TemplateCard from '../components/template/TemplateCard';
import { getFavoriteTemplates } from '../services/api';

const ITEMS_PER_PAGE = 12;

export default function FavoriteTemplatesPage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [sortOrder, setSortOrder] = useState('최신 순');
    const [templates, setTemplates] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const { workspaceId } = useParams();

    useEffect(() => {
        const fetchFavorites = async () => {
            if (!workspaceId) return;

            try {
                const pageable = {
                    page: currentPage - 1,
                    size: ITEMS_PER_PAGE,
                    // sort: sortOrder === '최신 순' ? 'createdAt,desc' : 'popularity,desc' // 백엔드 정렬 파라미터 확인 필요
                };
                const response = await getFavoriteTemplates(workspaceId, null, pageable);
                setTemplates(response.data.content);
                setTotalPages(response.data.totalPages);
            } catch (error) {
                console.error('Error fetching favorite templates:', error);
            }
        };

        fetchFavorites();
    }, [workspaceId, currentPage, sortOrder]);

    const handleSearch = (query) => {
        setSearchQuery(query);
        setCurrentPage(1);
    };

    const handlePageChange = (event, value) => {
        setCurrentPage(value);
    };

    const handleSortChange = (event) => {
        setSortOrder(event.target.value);
    };
    
    // 검색어에 따라 템플릿 필터링 (클라이언트 측)
    const filteredTemplates = templates.filter(t => 
        t.templateTitle.toLowerCase().includes(searchQuery.toLowerCase())
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

                <Box sx={{ width: '100%', flexGrow: 1, overflow: 'auto', p: 0.5 }}>
                    <Box
                        sx={{
                            display: 'flex',
                            flexWrap: 'wrap',
                        }}
                    >
                        {filteredTemplates.map(template => (
                            <Box
                                key={template.favoriteId}
                                sx={{
                                    flex: '0 0 25%',
                                    boxSizing: 'border-box',
                                    p: 1,
                                }}
                            >
                                <TemplateCard template={{
                                    id: template.templateId,
                                    title: template.templateTitle,
                                    content: template.templateContent,
                                }} />
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