import React, { useState, useMemo } from 'react';
import { Box, Tabs, Tab, Button, FormControl, Select, MenuItem, CssBaseline } from '@mui/material';
import Sidebar from '../components/layout/Sidebar';
import TemplateList from '../components/template/TemplateList';
// 1. 필요한 공통 컴포넌트들을 모두 import 합니다.
import SearchInput from "../components/common/SearchInput";
import Pagination from "../components/common/Pagination";
import TemplateCard from "../components/template/TemplateCard";

const mockTemplates = [
    { id: 1, title: '템플릿 1', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사중' },
    { id: 2, title: '템플릿 2', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사요청' },
    { id: 3, title: '템플릿 3', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사완료' },
    { id: 4, title: '템플릿 4', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '반려' },
    { id: 5, title: '템플릿 5', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사요청' },
    { id: 6, title: '템플릿 6', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사중' },
    { id: 7, title: '템플릿 7', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사중' },
    { id: 8, title: '템플릿 8', content: '템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용템플릿 내용', status: '심사요청' },
    // 페이징 테스트를 위해 데이터 추가
    { id: 9, title: '검색용 템플릿 9', content: '템플릿 내용', status: '심사완료' },
    { id: 10, title: '템플릿 10', content: '템플릿 내용', status: '반려' },
    { id: 11, title: '템플릿 11', content: '템플릿 내용', status: '심사요청' },
    { id: 12, title: '템플릿 12', content: '템플릿 내용', status: '심사중' },
];
const tabLabels = ['전체', '심사중', '심사완료', '반려'];
const ITEMS_PER_PAGE = 8; // 페이지 당 보여줄 템플릿 개수

export default function TemplatePage() {
    const [tabValue, setTabValue] = React.useState(0);
    const [sortOrder, setSortOrder] = React.useState('최신 순');

    // 2. 검색어와 현재 페이지를 관리할 state를 추가합니다.
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);

    const handleTabChange = (event, newValue) => {
        setTabValue(newValue);
        setCurrentPage(1); // 탭 변경 시 1페이지로 초기화
    };
    const handleSortChange = (event) => {
        setSortOrder(event.target.value);
    };

    // 3. 자식 컴포넌트(SearchInput, Pagination)에서 사용할 핸들러 함수를 정의합니다.
    const handleSearch = (query) => {
        setSearchQuery(query);
        setCurrentPage(1); // 검색 시 1페이지로 초기화
    };
    const handlePageChange = (event, value) => {
        setCurrentPage(value);
    };

    // 4. 기존 filteredTemplates 로직을 탭과 검색을 모두 처리하도록 확장합니다.
    const finalFilteredTemplates = useMemo(() => {
        let templates = mockTemplates;

        // 탭 필터링
        if (tabValue !== 0) {
            templates = templates.filter(t => t.status === tabLabels[tabValue]);
        }

        // 검색어 필터링
        if (searchQuery) {
            templates = templates.filter(t =>
                t.title.toLowerCase().includes(searchQuery.toLowerCase())
            );
        }

        return templates;
    }, [tabValue, searchQuery]); // tabValue 또는 searchQuery가 변경될 때만 재계산됩니다.

    // 5. 필터링된 결과를 바탕으로 페이징 처리를 합니다.
    const totalPages = Math.ceil(finalFilteredTemplates.length / ITEMS_PER_PAGE);
    const paginatedTemplates = finalFilteredTemplates.slice(
        (currentPage - 1) * ITEMS_PER_PAGE,
        currentPage * ITEMS_PER_PAGE
    );

    return (
        <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
            <CssBaseline />
            <Sidebar/>

            <Box component="main" sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column', height: '100vh', overflowY: 'auto' }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexShrink: 0 }}>
                    {/* 탭 & 템플릿 제작 버튼 */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <Tabs value={tabValue} onChange={handleTabChange}>
                            {tabLabels.map(label => <Tab key={label} label={label} />)}
                        </Tabs>
                        <Button variant="contained" sx={{ bgcolor: '#343a40', color: 'white', boxShadow: 'none', '&:hover': { bgcolor: '#495057' } }}>
                            템플릿 제작
                        </Button>
                    </Box>
                    {/* 검색 & 정렬 */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        {/* 6. SearchInput 컴포넌트를 여기에 배치하고 onSearch 핸들러를 전달합니다. */}
                        <SearchInput onSearch={handleSearch} />
                        <FormControl size="small">
                            <Select value={sortOrder} onChange={handleSortChange}>
                                <MenuItem value={'최신 순'}>최신 순</MenuItem>
                                <MenuItem value={'가나다 순'}>가나다 순</MenuItem>
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
                {/* 7. TemplateList에는 페이징 처리까지 완료된 데이터를 전달합니다. */}
                {/*<Box sx={{ flexGrow: 1, overflow: 'auto' }}>*/}
                {/*    <TemplateList templates={paginatedTemplates} />*/}
                {/*</Box>*/}
                {/* 8. Pagination 컴포넌트를 여기에 배치하고 필요한 props를 전달합니다. */}
                <Pagination
                    count={totalPages}
                    page={currentPage}
                    onChange={handlePageChange}
                />
            </Box>
        </Box>
    );
}