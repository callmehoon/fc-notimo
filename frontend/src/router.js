import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import SignUp from './pages/SignUp';
import FindPassword from "./pages/FindPassword";
import WorkspaceSelection from "./pages/WorkspaceSelection";
import CreateWorkspace from "./pages/CreateWorkspace";
import MyTemplatePage from "./pages/MyTemplatePage";
import PublicTemplatePage from "./pages/PublicTemplatePage";
import TemplateGeneratorPage from "./pages/TemplateGeneratorPage";
import FavoriteTemplatesPage from "./pages/FavoriteTemplatesPage";
import ContactManagementPage from "./pages/ContactManagementPage";
import ContactEditPage from "./pages/ContactEditPage";
import UserProfileEditPage from "./pages/UserProfileEditPage";
import WorkspaceEditPage from "./pages/WorkspaceEditPage";
import SocialLoginCallback from "./pages/SocialLoginCallback";
import SocialSignupPage from "./pages/SocialSignupPage";


const AppRouter = () => {
    return (
        <Routes>
            {/* 기본 경로는 로그인 페이지로 설정하거나 메인 페이지로 설정할 수 있습니다. */}
            <Route path="/" element={<Login/>}/>
            <Route path="/login" element={<Login/>}/>
            <Route path="/signup" element={<SignUp/>}/>
            <Route path="/FindPassword" element={<FindPassword/>}/>
            <Route path="/workspace" element={<WorkspaceSelection/>}/>
            <Route path={"/createworkspace"} element={<CreateWorkspace/>}/>
            <Route path={"/mytemplate"} element={<MyTemplatePage/>}/>
            <Route path={"/publicTemplate"} element={<PublicTemplatePage/>}/>
            <Route path="/workspace/:workspaceId/templategenerator/:templateId" element={<TemplateGeneratorPage/>}/>
            <Route path="/favoritetemplates" element={<FavoriteTemplatesPage/>}/>
            <Route path="/contact-management" element={<ContactManagementPage/>}/>
            <Route path="/contact-edit/:id" element={<ContactEditPage/>}/>
            <Route path="/profile-edit" element={<UserProfileEditPage/>}/>
            <Route path="/workspace-edit/:id" element={<WorkspaceEditPage/>}/>
            <Route path="/social-callback" element={<SocialLoginCallback/>}/>
            <Route path="/social-signup" element={<SocialSignupPage/>}/>
            {/* 예: 메인 페이지 라우트 */}
            {/* <Route path="/main" element={<MainPage />} /> */}
        </Routes>
    );
};

export default AppRouter;
