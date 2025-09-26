import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
import CommonButton from '../components/button/CommonButton';
import ErrorBoundary from '../components/common/ErrorBoundary';
import { getPublicTemplates, createIndividualTemplateFromPublic, deletePublicTemplate } from '../services/publicTemplateService';
import { addPublicTemplateToFavorites, removePublicTemplateFromFavorites, getFavoriteTemplates } from '../services/favoriteService';

const ITEMS_PER_PAGE = 8;

export default function PublicTemplatePage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [sortOrder, setSortOrder] = useState('createdAt,desc'); // API-compatible sort order
    const [templates, setTemplates] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
    const [favoriteTemplates, setFavoriteTemplates] = useState(new Set()); // 즐겨찾기 템플릿 ID 저장
    const navigate = useNavigate();

    useEffect(() => {
        const fetchTemplates = async () => {
            try {
                const [sortField, sortDirection] = sortOrder.split(',');
                const pageable = {
                    page: currentPage - 1,
                    size: ITEMS_PER_PAGE,
                    sort: sortField,
                    direction: sortDirection.toUpperCase(),
                };

                // 검색어가 있으면 추가
                if (searchQuery.trim()) {
                    pageable.q = searchQuery.trim();
                }

                const response = await getPublicTemplates(pageable);
                setTemplates(response.data.content);
                setTotalPages(response.data.totalPages);
            } catch (error) {
                console.error("Failed to fetch public templates:", error);
                // Handle error appropriately
            }
        };

        const fetchFavorites = async () => {
            try {
                const workspaceId = localStorage.getItem('selectedWorkspaceId');
                if (workspaceId) {
                    const favoriteResponse = await getFavoriteTemplates(workspaceId, 'PUBLIC', { page: 0, size: 1000 });
                    const favoriteIds = new Set(
                        favoriteResponse.data.content?.map(fav => fav.publicTemplateId).filter(Boolean) || []
                    );
                    setFavoriteTemplates(favoriteIds);
                }
            } catch (error) {
                console.error("Failed to fetch favorites:", error);
                console.error("즐겨찾기 로드 실패 상세:", {
                    message: error.message,
                    response: error.response?.data,
                    status: error.response?.status,
                    url: error.response?.config?.url
                });
                // 즐겨찾기 로드 실패해도 빈 Set으로 초기화하여 앱 동작 계속
                setFavoriteTemplates(new Set());
            }
        };

        fetchTemplates();
        fetchFavorites();
    }, [currentPage, sortOrder, searchQuery]);

    const handleSearch = (query) => { setSearchQuery(query); setCurrentPage(1); };
    const handlePageChange = (event, value) => { setCurrentPage(value); };
    const handleSortChange = (event) => { setSortOrder(event.target.value); };

    const handleUseTemplate = async (publicTemplateId) => {
        const workspaceId = localStorage.getItem('selectedWorkspaceId');
        if (!workspaceId) {
            alert('Please select a workspace first.');
            return;
        }

        try {
            const response = await createIndividualTemplateFromPublic(workspaceId, publicTemplateId);
            const newTemplateId = response.data.individualTemplateId;
            navigate(`/mytemplate`);
        } catch (error) {
            console.error("Failed to create template from public:", error);
            alert('Failed to use template.');
        }
    };

    const handleDeleteTemplate = async (templateId) => {
        if (window.confirm('정말로 이 템플릿을 삭제하시겠습니까?')) {
            try {
                await deletePublicTemplate(templateId);
                setTemplates(prevTemplates => prevTemplates.filter(t => t.publicTemplateId !== templateId));
                alert('템플릿이 삭제되었습니다.');
            } catch (error) {
                console.error("Failed to delete template:", error);
                alert('템플릿 삭제에 실패했습니다.');
            }
        }
    };

    const handleFavoriteToggle = async (templateId) => {
        const workspaceId = localStorage.getItem('selectedWorkspaceId');
        if (!workspaceId) {
            alert('워크스페이스를 선택해주세요.');
            return;
        }

        const isFavorite = favoriteTemplates.has(templateId);

        if (isFavorite) {
            // 이미 즐겨찾기에 있으면 삭제는 즐겨찾기 페이지에서만
            alert('즐겨찾기 해제는 즐겨찾기 페이지에서 가능합니다.');
            return;
        }

        // 즐겨찾기 추가는 여기서 가능
        try {
            await addPublicTemplateToFavorites(workspaceId, templateId);
            setFavoriteTemplates(prev => new Set(prev).add(templateId));
            console.log('공용 템플릿 즐겨찾기에 추가됨:', templateId);
        } catch (error) {
            console.error("즐겨찾기 추가 실패:", error);

            let errorMessage = '즐겨찾기 추가 중 오류가 발생했습니다.';
            if (error.response?.status === 404) {
                errorMessage = '템플릿을 찾을 수 없습니다.';
            } else if (error.response?.status === 401) {
                errorMessage = '로그인이 필요합니다.';
            } else if (error.response?.status === 409) {
                errorMessage = '이미 즐겨찾기에 등록된 템플릿입니다.';
            } else if (error.response?.data?.message) {
                errorMessage = error.response.data.message;
            }

            alert(errorMessage);
        }
    };

    return (
        <ErrorBoundary>
            <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
                <CssBaseline />
                <Sidebar />

            <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexShrink: 0 }}>
                    
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <SearchInput onSearch={handleSearch} />
                        <FormControl size="small" sx={{ minWidth: 120 }}>
                            <Select value={sortOrder} onChange={handleSortChange}>
                                <MenuItem value={'createdAt,desc'}>최신 순</MenuItem>
                                <MenuItem value={'shareCount,desc'}>공유 순</MenuItem>
                                <MenuItem value={'publicTemplateTitle,asc'}>가나다 순</MenuItem>
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
                        {templates && templates.length > 0 ? templates.map(template => (
                            <TemplateCard
                                key={template.publicTemplateId}
                                template={template}
                                onUse={() => handleUseTemplate(template.publicTemplateId)}
                                onDelete={() => handleDeleteTemplate(template.publicTemplateId)}
                                onFavorite={() => handleFavoriteToggle(template.publicTemplateId)}
                                isFavorite={favoriteTemplates.has(template.publicTemplateId)}
                                isPublicTemplate={true}
                            />
                        )) : (
                            <div>템플릿이 없습니다.</div>
                        )}
                    </Box>
                </Box>

                <Pagination
                    count={totalPages}
                    page={currentPage}
                    onChange={handlePageChange}
                />
            </Box>
        </Box>
        </ErrorBoundary>
    );
}
