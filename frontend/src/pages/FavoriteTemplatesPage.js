// src/pages/FavoriteTemplatesPage.js
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import {
    Box, CssBaseline, FormControl, Select, MenuItem, Tabs, Tab
} from '@mui/material';

import Sidebar from '../components/layout/Sidebar';
import WorkspaceList from '../components/layout/WorkspaceList';
import SearchInput from '../components/common/SearchInput';
import Pagination from '../components/common/Pagination';
import TemplateCard from '../components/template/TemplateCard';
import { getFavoriteTemplates, removeFavorite } from '../services/favoriteService';
import MainLayout from "../components/layout/MainLayout";

const ITEMS_PER_PAGE = 12;

// 탭 매핑
const TAB_TO_TEMPLATE_TYPE = {
    0: null,          // 전체
    1: 'PUBLIC',      // 공용 템플릿
    2: 'INDIVIDUAL'   // 나의 템플릿
};

const tabLabels = ['전체', '공용 템플릿', '나의 템플릿'];

export default function FavoriteTemplatesPage() {
    const params = useParams();
    // 라우터에 :workspaceId가 없으면 localStorage에서 보조
    const [workspaceId, setWorkspaceId] = useState(() => params.workspaceId ?? localStorage.getItem('selectedWorkspaceId'));

    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [sortOrder, setSortOrder] = useState('최신 순');
    const [tabValue, setTabValue] = useState(0); // 탭 상태 추가
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

                // 탭에 따른 템플릿 타입 설정
                const templateType = TAB_TO_TEMPLATE_TYPE[tabValue];
                const res = await getFavoriteTemplates(workspaceId, templateType, pageable);

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
    }, [workspaceId, currentPage, sortOrder, tabValue]);

    // localStorage 변경 감지
    useEffect(() => {
        const handleStorageChange = () => {
            const newWorkspaceId = localStorage.getItem('selectedWorkspaceId');
            if (newWorkspaceId && newWorkspaceId !== workspaceId) {
                setWorkspaceId(newWorkspaceId);
            }
        };

        window.addEventListener('storage', handleStorageChange);

        // 같은 탭에서의 변경도 감지하기 위해 주기적으로 체크
        const interval = setInterval(handleStorageChange, 100);

        return () => {
            window.removeEventListener('storage', handleStorageChange);
            clearInterval(interval);
        };
    }, [workspaceId]);

    const handleSearch = (q) => { setSearchQuery(q); setCurrentPage(1); };
    const handlePageChange = (_, v) => setCurrentPage(v);
    const handleSortChange = (e) => setSortOrder(e.target.value);
    const handleTabChange = (_, newValue) => { setTabValue(newValue); setCurrentPage(1); };

    const handleRemoveFavorite = async (favoriteId) => {
        if (window.confirm('즐겨찾기에서 제거하시겠습니까?')) {
            try {
                await removeFavorite(favoriteId);
                // 성공시 해당 템플릿을 목록에서 제거
                setTemplates(prevTemplates =>
                    prevTemplates.filter(template => template.favoriteId !== favoriteId)
                );
                alert('즐겨찾기에서 제거되었습니다.');
            } catch (error) {
                console.error('즐겨찾기 제거 실패:', error);
                alert('즐겨찾기 제거에 실패했습니다.');
            }
        }
    };

    // 템플릿 제목이 null일 수도 있으니 안전 처리
    const filteredTemplates = templates.filter(t =>
        (t.templateTitle ?? '').toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <MainLayout>
            <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
                <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflow: 'hidden' }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexShrink: 0 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <Tabs value={tabValue} onChange={handleTabChange}>
                                {tabLabels.map((label, index) => (
                                    <Tab key={index} label={label} />
                                ))}
                            </Tabs>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <SearchInput onSearch={handleSearch} />
                            <FormControl size="small" sx={{ minWidth: 120 }}>
                                <Select value={sortOrder} onChange={handleSortChange}>
                                    <MenuItem value={'최신 순'}>최신 순</MenuItem>
                                    <MenuItem value={'가나다 순'}>가나다 순</MenuItem>
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
                            {filteredTemplates.length > 0 ? (
                                filteredTemplates.map(favoriteItem => {
                                    // TemplateCard가 기대하는 데이터 구조로 변환합니다.
                                    const cardTemplateProps = {
                                        // 공용/개인 템플릿의 원래 ID와 제목, 내용을 전달합니다.
                                        // TemplateCard는 individualTemplateTitle, publicTemplateTitle 등을 우선적으로 사용합니다.
                                        ...(favoriteItem.templateType === 'INDIVIDUAL' && {
                                            individualTemplateId: favoriteItem.templateId,
                                            individualTemplateTitle: favoriteItem.templateTitle,
                                            individualTemplateContent: favoriteItem.templateContent,
                                        }),
                                        ...(favoriteItem.templateType === 'PUBLIC' && {
                                            publicTemplateId: favoriteItem.templateId,
                                            publicTemplateTitle: favoriteItem.templateTitle,
                                            publicTemplateContent: favoriteItem.templateContent,
                                            buttonTitle: favoriteItem.buttonTitle,
                                        }),
                                    };

                                    return (
                                        <TemplateCard
                                            key={favoriteItem.favoriteId}
                                            template={cardTemplateProps}
                                            // 즐겨찾기 해제는 onFavorite 핸들러를 사용합니다.
                                            onFavorite={() => handleRemoveFavorite(favoriteItem.favoriteId)}
                                            isFavorite={true}
                                            // 템플릿 타입에 따라 isPublicTemplate 값을 동적으로 설정합니다.
                                            isPublicTemplate={favoriteItem.templateType === 'PUBLIC'}
                                            // 즐겨찾기 페이지에서는 수정/공유/삭제 액션을 보여주지 않습니다.
                                            showActions={false}
                                        />
                                    );
                                })
                            ) : (
                                <Box sx={{
                                    gridColumn: '1 / -1',
                                    textAlign: 'center',
                                    py: 8,
                                    color: 'text.secondary'
                                }}>
                                    즐겨찾기한 템플릿이 없습니다.
                                </Box>
                            )}
                        </Box>
                    </Box>

                    <Pagination count={totalPages} page={currentPage} onChange={handlePageChange} />
                </Box>
            </Box>
        </MainLayout>
    );
}
