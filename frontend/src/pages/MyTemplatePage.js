// src/pages/TemplatePage.jsx
import React, { useState, useEffect } from 'react';
import { Box, Tabs, Tab, Button, FormControl, Select, MenuItem, CssBaseline, CircularProgress } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/layout/Sidebar';
import SearchInput from "../components/common/SearchInput";
import Pagination from "../components/common/Pagination";
import TemplateCard from "../components/template/TemplateCard";
import { listMyTemplates, deleteMyTemplate, shareMyTemplate, getMyTemplate, getTemplateHistories } from '../services/individualTemplateService';
import { addIndividualTemplateToFavorites, removeIndividualTemplateFromFavorites, getFavoriteTemplates } from '../services/favoriteService';
import MainLayout from "../components/layout/MainLayout";

const ITEMS_PER_PAGE = 8;
const tabLabels = ['전체', '심사중', '심사완료', '반려'];

// 정렬 매핑
const SORT_MAP = {
    '최신 순': { sortType: 'latest', direction: 'desc' },
    '가나다 순': { sortType: 'title',  direction: 'asc'  },
    '공유 순':   { sortType: 'title',  direction: 'desc' }, // 백엔드에 없으면 임시
};

// 상태 매핑 (Swagger의 실제 Enum 이름으로 교체)
const TAB_TO_STATUS = { 0: 'DRAFT', 1: 'PENDING', 2: 'APPROVED', 3: 'REJECTED' };

export default function TemplatePage() {
    const navigate = useNavigate();

    // 워크스페이스 id 관리(프로젝트 상황에 맞게 가져오세요)
    const [workspaceId, setWorkspaceId] = useState(() => localStorage.getItem('selectedWorkspaceId') || 1);

    const [tabValue, setTabValue] = useState(0);
    const [sortOrder, setSortOrder] = useState('최신 순');
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);

    const [rows, setRows] = useState([]);
    const [total, setTotal] = useState(0);
    const [loading, setLoading] = useState(false);
    const [err, setErr] = useState(null);
    const [favoriteTemplates, setFavoriteTemplates] = useState(new Set()); // 즐겨찾기 템플릿 ID 저장

    const load = async () => {
        setLoading(true); setErr(null);
        try {
            const { sortType, direction } = SORT_MAP[sortOrder] || SORT_MAP['최신 순'];
            const status = TAB_TO_STATUS[tabValue] || null;

            const res = await listMyTemplates({
                workspaceId,
                page: currentPage - 1, // 0-based
                size: ITEMS_PER_PAGE,
                sortType,
                direction,
                status,
                q: searchQuery,
            });

            const data = res.data;
            setRows(data.content ?? []);
            setTotal(data.totalElements ?? 0);
        } catch (e) {
            console.error(e);
            setErr('목록을 불러오는 데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const loadFavorites = async () => {
        try {
            if (workspaceId) {
                const favoriteResponse = await getFavoriteTemplates(workspaceId, 'INDIVIDUAL', { page: 0, size: 100 });
                const favoriteIds = new Set(
                    favoriteResponse.data.content?.map(fav => fav.individualTemplateId).filter(Boolean) || []
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

    useEffect(() => {
        load();
        loadFavorites();
        /* eslint-disable-next-line */
    }, [workspaceId, tabValue, sortOrder, searchQuery, currentPage]);

    const handleTabChange = (_, v) => { setTabValue(v); setCurrentPage(1); };
    const handleSortChange = (e) => { setSortOrder(e.target.value); setCurrentPage(1); };
    const handleSearch = (q) => { setSearchQuery(q); setCurrentPage(1); };
    const handlePageChange = (_, v) => setCurrentPage(v);

    const handleDelete = async (templateId) => {
        if (!window.confirm('이 템플릿을 삭제할까요?')) return;
        try {
            await deleteMyTemplate(workspaceId, templateId);
            await load();
        } catch (e) {
            alert('삭제에 실패했습니다.');
        }
    };

    const handleShare = async (templateId) => {
        try {
            await shareMyTemplate(workspaceId, templateId);
            alert('공유되었습니다.');
            // 필요 시 await load();
        } catch (e) {
            alert('공유에 실패했습니다.');
        }
    };

    const handleEdit = async (templateId) => {
        try {
            // 템플릿 기본 정보와 채팅 이력을 동시에 조회
            const [templateResponse, historiesResponse] = await Promise.all([
                getMyTemplate(workspaceId, templateId),
                getTemplateHistories(workspaceId, templateId)
            ]);

            const templateData = templateResponse.data;
            const histories = historiesResponse.data;

            // 템플릿 생성 페이지로 데이터와 함께 이동
            navigate(`/workspace/${workspaceId}/templategenerator/${templateId}`, {
                state: {
                    templateData,
                    chatHistories: histories,
                    isEdit: true
                }
            });
        } catch (error) {
            console.error('Failed to load template for editing:', error);
            const errorMessage = error.response?.data?.message || error.message || '템플릿 로딩에 실패했습니다.';
            alert(errorMessage);
        }
    };

    const handleFavoriteToggle = async (templateId) => {
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
            await addIndividualTemplateToFavorites(workspaceId, templateId);
            setFavoriteTemplates(prev => new Set(prev).add(templateId));
            console.log('개인 템플릿 즐겨찾기에 추가됨:', templateId);
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

    const totalPages = Math.ceil(total / ITEMS_PER_PAGE);

    return (
        <MainLayout>
            <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
                <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflowY: 'auto' }}>
                    {/* 상단 바 */}
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexShrink: 0 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <Tabs value={tabValue} onChange={handleTabChange}>
                                {tabLabels.map(label => <Tab key={label} label={label} />)}
                            </Tabs>
                            <Button
                                variant="contained"
                                sx={{ bgcolor: '#343a40', color: 'white', boxShadow: 'none', '&:hover': { bgcolor: '#495057' } }}
                                onClick={() => navigate(`/workspace/${workspaceId}/templategenerator/new`)}
                            >
                                템플릿 제작
                            </Button>
                        </Box>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <SearchInput onSearch={handleSearch} />
                            <FormControl size="small">
                                <Select value={sortOrder} onChange={handleSortChange}>
                                    <MenuItem value={'최신 순'}>최신 순</MenuItem>
                                    <MenuItem value={'가나다 순'}>가나다 순</MenuItem>
                                </Select>
                            </FormControl>
                        </Box>
                    </Box>

                    {/* 리스트 */}
                    <Box sx={{ width: '100%', flexGrow: 1, overflow: 'auto', p: 1 }}>
                        {loading ? (
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}><CircularProgress /></Box>
                        ) : err ? (
                            <Box sx={{ color: 'error.main' }}>{err}</Box>
                        ) : (
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
                                {rows.map(t => (
                                    <TemplateCard
                                        key={t.individualTemplateId}
                                        template={t}
                                        onDelete={() => handleDelete(t.individualTemplateId)}
                                        onShare={() => handleShare(t.individualTemplateId)}
                                        onEdit={() => handleEdit(t.individualTemplateId)}
                                        onFavorite={() => handleFavoriteToggle(t.individualTemplateId)}
                                        isFavorite={favoriteTemplates.has(t.individualTemplateId)}
                                        showActions
                                        isPublicTemplate={false}
                                    />
                                ))}
                            </Box>
                        )}
                    </Box>
                    <Pagination count={totalPages} page={currentPage} onChange={handlePageChange} />
                </Box>
            </Box>
        </MainLayout>
    );
}
