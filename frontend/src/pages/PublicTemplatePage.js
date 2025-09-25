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
import { getPublicTemplates, createIndividualTemplateFromPublic, deletePublicTemplate } from '../services/publicTemplateService';

const ITEMS_PER_PAGE = 12;

export default function PublicTemplatePage() {
    const [searchQuery, setSearchQuery] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [sortOrder, setSortOrder] = useState('createdAt,desc'); // API-compatible sort order
    const [templates, setTemplates] = useState([]);
    const [totalPages, setTotalPages] = useState(0);
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

        fetchTemplates();
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
            const newTemplateId = response.data.id; // Assuming the response contains the new template with its id
            navigate(`/workspace/${workspaceId}/templategenerator/${newTemplateId}`);
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

    return (
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
                        {templates.map(template => (
                            <TemplateCard 
                                key={template.publicTemplateId}
                                template={template} 
                                onUse={() => handleUseTemplate(template.publicTemplateId)} 
                                onDelete={() => handleDeleteTemplate(template.publicTemplateId)}
                                isPublicTemplate={true}
                            />
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
